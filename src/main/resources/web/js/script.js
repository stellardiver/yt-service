var myButton = document.getElementsByClassName('btn-primary')[0];
var form = document.getElementsByClassName('form')[0];

form.addEventListener(
    'submit',
     function(event) {
        event.preventDefault();
        submitForm();
    }
);

function submitForm() {

    var login = document.getElementsByClassName('input-login')[0].value
    var password = document.getElementsByClassName('input-password')[0].value;

    if (login.trim() == "" || password.trim() == "") {
        alert("Поля не должны быть пустыми");
        return false;
    }

    var xhr = new XMLHttpRequest();
    var url = "/api/v1/login";
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {

          var response = JSON.parse(xhr.responseText);

          setCookie("TOKEN", response.token, 7);
          window.location.href = "/yt_trending_videos";

        } else if (xhr.readyState === 4 && xhr.status === 401) {
            alert("Неверный логин или пароль");
        } else if (xhr.readyState === 4 && xhr.status === 404) {
            alert("Пользователь не найден");
        }
    };

    var data = JSON.stringify({ login: login, password: password });
    xhr.send(data);
}

function getCookie(name) {
    var dc = document.cookie;
    var prefix = name +"=";
    var begin = dc.indexOf("; " + prefix);
    if (begin == -1) {
        begin = dc.indexOf(prefix);
        if (begin != 0)return null;
    } else {
        begin += 2;
    }
    var end = document.cookie.indexOf(";", begin);
    if (end == -1) {
        end = dc.length;
    }
    return unescape(dc.substring(begin + prefix.length, end));
}

function setCookie(c_name, value, expireDays) {
    var expireDate = new Date();
    expireDate.setDate(expireDate.getDate() + expireDays);
    document.cookie = c_name + "=" + value + ";path=/" + ((expireDays == null) ? "" : ";expires=" + expireDate.toGMTString());
}