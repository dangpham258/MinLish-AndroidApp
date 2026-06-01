package com.minlish.app.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor() {
    operator fun invoke(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
}
