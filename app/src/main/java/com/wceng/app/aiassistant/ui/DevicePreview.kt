package com.wceng.app.aiassistant.ui

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview


@Preview(showBackground = true, device = Devices.PHONE, name = "Phone")
@Preview(showBackground = true, device = Devices.TABLET, name = "Tablet")
@Preview(showBackground = true, device = Devices.FOLDABLE, name = "Foldable")
//@Preview(showBackground = true, device = Devices.DESKTOP, name = "Desktop")
annotation class DevicePreview