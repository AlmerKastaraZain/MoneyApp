package com.telkom.DanaApp.view.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.telkom.DanaApp.R
import com.telkom.DanaApp.component.ComponentInputBar
import com.telkom.DanaApp.component.ModernRoundedButton
import com.telkom.DanaApp.ui.theme.InputType
import com.telkom.DanaApp.ui.theme.Orange
import com.telkom.DanaApp.ui.theme.PoppinsFontFamily
import com.telkom.DanaApp.ui.theme.White



@Composable()
fun Register(
    onGoToLoginClicked: () -> Unit,
    onRegister: (email: String, password: String, confirmPassword: String, firstName: String, lastName: String) -> Unit,
) {
    var showSnackbar by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_wobble),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp)) // Adjust the spacer height as needed
            Image(
                painter = painterResource(id = R.drawable.icon_guest),
                contentDescription = "Guest Icon",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(160.dp, 160.dp),
            )
            Text(
                text = stringResource(id = R.string.SignUpText),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 32.dp),
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Normal,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp)

            )
            Spacer(modifier = Modifier.height(10.dp)) // Add some padding at the bottom


            Column(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)) {
                var inputPasswordTextValue = ""
                var inputConfirmPasswordTextValue = ""
                var inputEmailTextValue = ""
                var inputFirstNameTextValue = ""
                var inputLastNameTextValue = ""
                val inputEmailText = remember { mutableStateOf("") }
                ComponentInputBar(
                    placeholder = stringResource(id = R.string.InputEmail),
                    onTextChanged = { newText ->
                        inputEmailText.value = newText
                        inputEmailTextValue = newText
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .height(52.dp),
                    fontSize = 12,
                    inputType = InputType.EMAIL
                )
                Row(modifier = Modifier.fillMaxWidth()) { // Enclosing Row
                    val inputFirstNameText = remember { mutableStateOf("") }
                    val firstNameErrorState = remember {
                        derivedStateOf {
                            if (inputFirstNameText.value.length > 20) {
                                "Name Too long"
                            } else {
                                null
                            }
                        }
                    }
                    ComponentInputBar(
                        placeholder = stringResource(id = R.string.InputFirstName),
                        onTextChanged = { newText ->
                            inputFirstNameText.value = newText
                            inputFirstNameTextValue = newText
                        },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .weight(1f)
                            .height(52.dp),
                        fontSize = 12,
                        inputType = InputType.TEXT,
                        error = firstNameErrorState
                    )

                    val inputLastNameText = remember { mutableStateOf("") }
                    val lastNameErrorState = remember {
                        derivedStateOf {
                            if (inputLastNameText.value.length > 20) {
                                "Name too long"
                            } else {
                                null
                            }
                        }
                    }
                    ComponentInputBar(
                        placeholder = stringResource(id = R.string.InputLastName),
                        onTextChanged = { newText ->
                            inputLastNameText.value = newText
                            inputLastNameTextValue = newText
                        },
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .weight(1f)
                            .height(52.dp),
                        fontSize = 12,
                        inputType = InputType.TEXT,
                        error = lastNameErrorState
                    )
                }
                val inputPasswordText = remember { mutableStateOf("") } // Move inside Column
                ComponentInputBar(
                    placeholder = stringResource(id = R.string.InputPassword),


                    onTextChanged = { newText ->
                        inputPasswordText.value = newText
                        inputPasswordTextValue = newText
                        // Do something with the text, e.g., update a ViewModel
                        println("Input text: $newText")
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(52.dp),
                    fontSize = 12,
                    inputType = InputType.PASSWORD,
                )


                val inputConfirmText = remember { mutableStateOf("") }
                var confirmPasswordError by remember { mutableStateOf<String?>(null) }

                val confirmPasswordErrorState = remember {
                    derivedStateOf {
                        confirmPasswordError
                    }
                }

                ComponentInputBar(
                    placeholder = stringResource(id = R.string.InputConfirm),
                    onTextChanged = { newText ->
                        inputConfirmText.value = newText
                        inputConfirmPasswordTextValue = newText
                        // Do something with the text, e.g., update a ViewModel                        // Do something with the text, e.g., update a ViewModel
                        println("Input text: $newText")
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(52.dp),
                    fontSize = 12,
                    inputType = InputType.PASSWORD,
                    error = confirmPasswordErrorState
                )
                Spacer(modifier = Modifier.weight(1f)) //Push content to top, and our text to bottom
                if(showSnackbar){
                    Snackbar(modifier = Modifier.padding(16.dp)) {
                        Text(text = "There's Empty field or Password not match",
                            modifier = Modifier.paddingFromBaseline(top = 20.dp))
                    }
                }

                ModernRoundedButton(
                    text = "Create Account",
                    onClick = {
                        inputEmailTextValue
                        inputFirstNameTextValue
                        inputLastNameTextValue
                        inputPasswordTextValue
                        inputConfirmPasswordTextValue

                        val auth = Firebase.auth
                        // Validator
                        var hasError = false
                        if (
                            (
                            inputEmailTextValue.isEmpty() ||
                            inputFirstNameTextValue.isEmpty() ||
                            inputLastNameTextValue.isEmpty() ||
                            inputPasswordTextValue.isEmpty() ||
                            inputConfirmPasswordTextValue.isEmpty())
                            )
                        {
                            hasError = true
                        }
                        else if (inputPasswordTextValue != inputConfirmPasswordTextValue)
                        {
                            hasError = true
                        }
                        if (hasError){
                            showSnackbar = true
                        }
                        else
                        {
                            showSnackbar = false

                            onRegister(
                                inputEmailTextValue, //email
                                inputPasswordTextValue, //password
                                inputConfirmPasswordTextValue, // confirmPassword
                                inputFirstNameTextValue, // firstName
                                inputLastNameTextValue, //lastName
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Make button take full width
                        .height(48.dp), // Set a specific height (optional)
                    backgroundColor = Orange, // Customize background color
                    contentColor = White,      // Customize text color
                    FontSize = 18,
                )

                Spacer(modifier = Modifier.weight(1f)) //Push content to top, and our text to bottom
                Row(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)) {
                    Text(
                        text = "Already created an Account?",
                        modifier = Modifier
                            .padding(end = 2.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            fontSize = 12.sp,
                        ),
                    )

                    Text(
                        text = "Sign In",
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .clickable {
                                onGoToLoginClicked()
                            },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 12.sp,
                        ),
                    )
                }
            }
        }
    }
}
