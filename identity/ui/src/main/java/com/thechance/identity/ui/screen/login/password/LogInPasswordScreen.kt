package com.thechance.identity.ui.screen.login.password

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.thechance.identity.ui.R
import com.thechance.identity.ui.composable.AuthButton
import com.thechance.identity.ui.composable.BackButton
import com.thechance.identity.ui.composable.PasswordInputText
import com.thechance.identity.ui.spacer.SpacerVertical24
import com.thechance.identity.ui.theme.LightPrimaryBlackColor
import com.thechance.identity.ui.theme.LightSecondaryBlackColor
import com.thechance.identity.ui.theme.Typography
import com.thechance.identity.viewmodel.login.LoginUIState
import com.thechance.identity.viewmodel.login.LoginViewModel

@Composable
fun LogInPasswordScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val state by viewModel.uiState.collectAsState()
    LogInPasswordContent(
        state = state,
        onChangePassword = viewModel::onChangePassword,
        onValidate = viewModel::onValidatePassword,
        onLogin = viewModel::onLogin,
        onClickBack = { navController.navigateUp() }
    )
}

@Composable
fun LogInPasswordContent(
    state: LoginUIState,
    onChangePassword: (String) -> Unit,
    onValidate: () -> Boolean,
    onLogin: () -> Unit,
    onClickBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        BackButton(onClick = onClickBack)

        SpacerVertical24()
        Text(
            text = stringResource(id = R.string.log_in),
            style = Typography.h1,
            color = LightPrimaryBlackColor,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        SpacerVertical24()
        Text(
            text = stringResource(id = R.string.your_password),
            style = Typography.subtitle2,
            color = LightSecondaryBlackColor,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(14.dp))
        PasswordInputText(
            placeHolder = stringResource(id = R.string.password_place_holder),
            text = state.password,
            onTextChange = onChangePassword
        )

        SpacerVertical24()
        AuthButton(
            onClick = onLogin,
            isEnabled = onValidate.invoke(),
            text = stringResource(id = R.string.log_in),
            buttonModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}