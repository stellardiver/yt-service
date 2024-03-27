package com.example.utils

import com.example.models.ServiceUser
import org.mindrot.jbcrypt.BCrypt

object Hashes {

    /**
     * Check if the password matches the User's password
     */
    fun checkPassword(attempt: String, serviceUser: ServiceUser) = BCrypt.checkpw(attempt, serviceUser.password)

    /**
     * Returns the hashed version of the supplied password
     */
    fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

}