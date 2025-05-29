package com.telkom.DanaApp.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telkom.DanaApp.R
import com.telkom.DanaApp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentInputBar(
    modifier: Modifier = Modifier,
    label: String? = null, // Add an optional label parameter
    placeholder: String = "Type something...",
    onTextChanged: (String) -> Unit,
    fontSize: Int = 14,
    inputType: InputType = InputType.TEXT,
    error: State<String?> = remember { mutableStateOf(null) }
) {
    var text by remember { mutableStateOf("") }
    var internalError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = text) {
        if (inputType == InputType.EMAIL) {
            val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
            val isEmailValid = emailRegex.toRegex().matches(text)
            internalError = if (!isEmailValid && text.isNotEmpty()) {
                "Wrong Email Format"
            } else {
                null
            }
        }
    }

    LaunchedEffect(key1 = error.value) {
        internalError = error.value ?: internalError
    }

    val textSize = fontSize
    var passwordVisible by remember { mutableStateOf(false) }
    val visualTransformation =
        if (inputType == InputType.PASSWORD && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = LocalTextStyle.current.copy(
                    fontSize = textSize.sp,
                    fontFamily = PoppinsFontFamily,
                    color = Black // Or your desired label color
                ),
                modifier = Modifier.padding(bottom = 4.dp) // Adjust spacing as needed
            )
        }

        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onTextChanged(it)
            },
            keyboardOptions = when (inputType) {
                InputType.EMAIL -> androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                )
                else -> androidx.compose.foundation.text.KeyboardOptions.Default
            },
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = LocalTextStyle.current.copy(
                        fontSize = textSize.sp,
                        fontFamily = PoppinsFontFamily,
                        color = Gray, // Example placeholder color
                    ),
                )
            },
            isError = internalError != null,
            shape = RoundedCornerShape(6.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Orange,
                unfocusedBorderColor = Gray,
                cursorColor = Orange,
                focusedLabelColor = Orange,
                unfocusedLabelColor = Gray,
                focusedTextColor = Black,
                errorBorderColor = Red,
                errorLabelColor = Red,
                errorTextColor = Red,
                unfocusedTextColor = Black,
                containerColor = White,
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = textSize.sp,
                fontFamily = PoppinsFontFamily,
                color = Black, // Example input text color
            ),
            trailingIcon = {
                if (inputType == InputType.PASSWORD) {
                    val image =
                        if (passwordVisible) R.drawable.eye_open else R.drawable.eye_closed
                    val description =
                        if (passwordVisible) "Hide password" else "Show password"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = image),
                            contentDescription = description,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { passwordVisible = !passwordVisible },
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            visualTransformation = visualTransformation
        )

    }
}