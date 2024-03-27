package com.example.workers

import com.example.*
import com.example.dao.UserDao
import com.example.dao.YTVideoDao
import com.example.dao.userDAO
import com.example.models.GrstResponse
import com.example.models.YTUser
import com.example.utils.*
import com.frogking.chromedriver.ChromeDriverBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.devtools.DevTools
import org.openqa.selenium.devtools.v118.network.Network
import org.openqa.selenium.remote.service.DriverFinder
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class YTUserDataCollectWorker private constructor(private val userRepository: UserDao) {

    private val _ytUserIterator = MutableStateFlow(0)
    private val _ytUsers = MutableStateFlow<List<YTUser>>(emptyList())
    private val _grstResponseBody = MutableStateFlow<String?>(null)
    private lateinit var chromeDriver: ChromeDriver

    val isWorking: AtomicBoolean = AtomicBoolean(false)

    companion object {

        @Volatile
        private var instance: YTUserDataCollectWorker? = null

        fun getInstance(repository: UserDao) =
            instance ?: synchronized(this) {
                instance ?: YTUserDataCollectWorker(repository).also { instance = it }
            }
    }

    init {

        _ytUserIterator
            .onEach { iterator ->

                println("YTUser iterator update: $iterator")

                if (iterator > 0) {
                    if (_ytUsers.value.size > iterator)
                        startWebDriver(ytUser = _ytUsers.value[iterator])
                    else {
                        _ytUserIterator.value = 0
                        _ytUsers.value = emptyList()
                        isWorking.set(false)
                    }
                }
            }
            .launchIn(appCoroutineScope)
    }

    fun collectYTUsersData() {

        println("Prepare to check if service is working...")

        if (isWorking.get()) return

        println("Service is available, starting...")

        isWorking.set(true)

        appCoroutineScope.launch {
            val ytUsers = userRepository.getAllYTUsers()
            _ytUsers.value = ytUsers
        }

        Thread.sleep(3000)

        if (_ytUsers.value.isNotEmpty()) {
            startWebDriver(ytUser = _ytUsers.value[_ytUserIterator.value])
        } else {
            println("YTUsers not found: ${_ytUsers.value.size}")
            isWorking.set(false)
        }
    }

    private fun startWebDriver(ytUser: YTUser) {

        val proxyAddress = "pr.oxylabs.io"
        val proxyPort = 7777
        val proxyUsername = "customer-ozan_zone17-cc--sessid-l5c0houomm-sesstime-3"
        val proxyPassword = "v5pi7YEqjS0tq9Jr"

        val proxyUrl = "https://$proxyAddress:$proxyPort"

        val options = ChromeOptions().apply {

            val prefs: MutableMap<String, Any> = HashMap()
            prefs["credentials_enable_service"] = false
            prefs["profile.password_manager_enabled"] = false

            setExperimentalOption("prefs", prefs)

            addArguments("--window-size=1920,1080")
            addArguments("--password-store=basic")
            addArguments("--incognito")
            //addArguments("--proxy-server=$proxyUrl")
            //addArguments("--headless=new")
        }

        chromeDriver = ChromeDriverBuilder().build(
            options,
            DriverFinder.getPath(
                ChromeDriverService.createDefaultService(),
                ChromeOptions()
            ).driverPath
        )

//        chromeDriver.register(
//            UsernameAndPassword.of(proxyUsername, proxyPassword)
//        )

        chromeDriver.get(STUDIO_YOUTUBE_URL)

        waitUntilPageIsReady(chromeDriver)

        if (chromeDriver.currentUrl.contains(GOOGLE_AUTH_HOST)) {

            // Произошел редирект на авторизацию

            val emailInput = chromeDriver.findElement(By.id("identifierId"))
            val buttonNext = chromeDriver.findElement(By.id("identifierNext")).findElement(By.tagName("button"))

            emailInput.sendKeys(ytUser.email)

            buttonNext.click()

            waitUntilPageIsReady(chromeDriver)

            if (!chromeDriver.currentUrl.contains("accounts.google.com/v3/signin/rejected")) {

                Thread.sleep(3000)

                val passwordInput = chromeDriver.findElement(By.className("Xb9hP")).findElement(By.tagName("input"))
                val passwordNextButton = chromeDriver.findElement(By.id("passwordNext")).findElement(By.tagName("button"))
                passwordInput.sendKeys(ytUser.password)

                passwordNextButton.click()

                waitUntilPageIsReady(chromeDriver)

                Thread.sleep(3000)

                if (chromeDriver.currentUrl.contains(SIGNIN_CHALLENGE_SELECTION_PATH)) {

                    // 2FA

                    val confirmRecoveryEmailButton = chromeDriver.findElement(By.xpath("//div[@data-challengeid='5']"))

                    confirmRecoveryEmailButton.click()

                    waitUntilPageIsReady(chromeDriver)

                    Thread.sleep(2000)

                    val recoveryEmailInput = chromeDriver.findElement(
                        By.xpath("//input[@name='knowledgePreregisteredEmailResponse']")
                    )
                    val recoveryNextButton = chromeDriver.findElement(
                        By.cssSelector(".FliLIb.DL0QTb")).findElement(
                        By.tagName("button")
                    )

                    recoveryEmailInput.sendKeys(ytUser.recoveryEmail)

                    recoveryNextButton.click()

                    waitUntilPageIsReady(chromeDriver)

                    Thread.sleep(2000)

                    if (chromeDriver.currentUrl.contains("gds.google.com/web/chip")) {

                        // Выйти из экрана suggestions

                        val buttonNotNow = chromeDriver.findElement(
                            By.className("lq3Znf")).findElement(
                            By.tagName("button")
                        )
                        buttonNotNow.click()

                        waitUntilPageIsReady(chromeDriver)
                    }
                }

                fireDevToolsNetworkListener(devTools = chromeDriver.devTools, ytUser = ytUser)

            } else {
                println("Signin rejected! Closing driver...")
                chromeDriver.close()
            }
        }
    }

    private fun waitUntilPageIsReady(driver: WebDriver) {
        val executor = driver as JavascriptExecutor
        WebDriverWait(driver, Duration.ofSeconds(10))
            .until { executor.executeScript("return document.readyState") == "complete" }
    }

    private fun fireDevToolsNetworkListener(devTools: DevTools, ytUser: YTUser) {

        runCatching {

            devTools.createSession()

            devTools.send(
                Network.enable(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
                )
            )

            devTools.addListener(
                Network.responseReceived()
            ) { entry ->

                if (entry.response.url.contains(ARS_GRST_PATH)) {

                    val body = devTools.send(Network.getResponseBody(entry.requestId)).body

                    _grstResponseBody.value = body

                    val grstResponse: GrstResponse? = Gson().fromJson<GrstResponse>(
                        body,
                        object : TypeToken<GrstResponse?>() {}.type
                    )

                    grstResponse?.let {
                        println(grstResponse)
                        ytUser.sessionToken = it.sessionToken
                    }

                    for (cookie in COOKIES_LIST) {
                        chromeDriver.manage().getCookieNamed(cookie)?.let {

                            println("Cookie $cookie, value: ${it.value}")

                            when (cookie) {
                                SID_COOKIE -> ytUser.sidCookie = it.value
                                HSID_COOKIE -> ytUser.hsidCookie = it.value
                                SSID_COOKIE -> ytUser.ssidCookie = it.value
                                APISID_COOKIE -> ytUser.apisidCookie = it.value
                                SAPISID_COOKIE -> ytUser.sapisidCookie = it.value
                                SECURE_1PSID_COOKIE -> ytUser.secure1psidCookie = it.value
                                SECURE_3PSID_COOKIE -> ytUser.secure3psidCookie = it.value
                            }
                        }
                    }

                    chromeDriver.close()

                    appCoroutineScope.launch {
                        userRepository.updateYTUser(ytUser)
                    }

                    _ytUserIterator.value += 1
                }
            }

        }.onFailure {
            println(it)
            chromeDriver.close()
            _ytUserIterator.value += 1
        }
    }
}

val ytUserDataCollectWorker = YTUserDataCollectWorker.getInstance(userDAO)