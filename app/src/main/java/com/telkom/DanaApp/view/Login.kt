package com.telkom.DanaApp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telkom.DanaApp.R
import com.telkom.DanaApp.component.ComponentInputBar
import com.telkom.DanaApp.component.ModernRoundedButton
import com.telkom.DanaApp.ui.theme.InputType
import com.telkom.DanaApp.ui.theme.Orange
import com.telkom.DanaApp.ui.theme.PoppinsFontFamily
import com.telkom.DanaApp.ui.theme.White

@Composable()
fun Login(
    onGoToRegisterClicked: () -> Unit,
    onLoginUsingFirebaseClicked: () -> Unit,
    onLoginEmailAndPassword: (email: String, password: String) -> Unit,
    loginErrorState: MutableState<String?>
) {

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
                modifier = Modifier.align(Alignment.CenterHorizontally).size(160.dp, 160.dp),
            )
            Text(
                text = stringResource(id = R.string.SignInText),
                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                val inputEmailText = remember { mutableStateOf("") }
                var inputEmailTextValue = ""

                val derivedLoginErrorState = remember {
                    derivedStateOf {
                        if (loginErrorState.value?.isNotEmpty() == true) {
                            "Invalid email or password"
                        } else {
                            null
                        }
                    }
                }
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
                    inputType = InputType.EMAIL,
                    error = derivedLoginErrorState
                )

                val inputPasswordText = remember { mutableStateOf("") }
                var inputPasswordTextValue = ""
                ComponentInputBar(
                    placeholder = stringResource(id = R.string.InputPassword),
                    onTextChanged = { newText ->
                        inputPasswordText.value = newText
                        inputPasswordTextValue = newText
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .height(52.dp),
                    fontSize = 12,
                    inputType = InputType.PASSWORD,
                    error = derivedLoginErrorState
                )

                // Show error message if login failed
                if (loginErrorState.value != null) {
                    Text(
                        text = loginErrorState.value!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            fontSize = 12.sp)
                        ,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot about your password?",
                        modifier = Modifier
                            .padding(end = 4.dp, top = 4.dp).alpha(.4f),  // You can adjust padding as needed
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            fontSize = 12.sp,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(26.dp)) // Add some padding at the bottom

                ModernRoundedButton(
                    text = "Login",
                    onClick = {
                        onLoginEmailAndPassword(inputEmailTextValue, inputPasswordTextValue)
                    },
                    modifier = Modifier
                        .fillMaxWidth() // Make button take full width
                        .height(48.dp), // Set a specific height (optional)
                    backgroundColor = Orange, // Customize background color
                    contentColor = White,      // Customize text color
                    FontSize = 18,
                )

                Spacer(modifier = Modifier.height(12.dp)) // Add some padding at the bottom

                Image(
                    painter = painterResource(id = R.drawable.or_slasher),
                    contentDescription = "Or slash",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 320.dp, height = 40.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shadowElevation = 8.dp, // Adjust elevation for shadow size
                        shape = RoundedCornerShape(6.dp), // Use a Material shape or a custom one
                        color = White, // Set your background color

                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_google),
                                contentDescription = "Google",
                                modifier = Modifier.padding(8.dp)
                                    .size(width = 48.dp, height = 48.dp)
                                    .background(White)
                                    .clickable {
                                        onLoginUsingFirebaseClicked()
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp)) // Add some padding at the bottom
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shadowElevation = 8.dp, // Adjust elevation for shadow size
                        shape = RoundedCornerShape(6.dp), // Use a Material shape or a custom one
                        color = White, // Set your background color

                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_linked_in),
                                contentDescription = "LinkedIn",
                                modifier = Modifier.padding(8.dp)
                                    .size(width = 48.dp, height = 48.dp).background(White)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp)) // Add some padding at the bottom
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shadowElevation = 8.dp, // Adjust elevation for shadow size
                        shape = RoundedCornerShape(6.dp), // Use a Material shape or a custom one
                        color = White, // Set your background color

                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_facebook),
                                contentDescription = "Facebook",
                                modifier = Modifier.padding(8.dp)
                                    .size(width = 48.dp, height = 48.dp).background(White)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f)) //Push content to top, and our text to bottom
                Row(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)) {
                    Text(
                        text = "Don't have an Account?",
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
                        text = "Sign Up",
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .clickable {
                                onGoToRegisterClicked()
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


