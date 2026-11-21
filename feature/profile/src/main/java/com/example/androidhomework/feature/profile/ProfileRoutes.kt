package com.example.androidhomework.feature.profile

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidhomework.feature.profile.ui.EditProfileScreen
import com.example.androidhomework.feature.profile.ui.EditProfileViewModel
import com.example.androidhomework.feature.profile.ui.ProfileScreen
import com.example.androidhomework.feature.profile.ui.ProfileViewModel


@Composable
fun ProfileRoute(onEditClick: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
    ProfileScreen(viewModel = viewModel, onEditClick = onEditClick)
}

@Composable
fun EditProfileRoute(
    onBackClick: () -> Unit,
    onDone: () -> Unit
) {
    val viewModel: EditProfileViewModel = viewModel(factory = EditProfileViewModel.Factory)
    EditProfileScreen(viewModel = viewModel, onBackClick = onBackClick, onDone = onDone)
}