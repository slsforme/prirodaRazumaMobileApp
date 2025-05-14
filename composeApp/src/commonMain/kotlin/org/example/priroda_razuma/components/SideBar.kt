package org.example.priroda_razuma.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.example.priroda_razuma.preferences.Theme
import org.jetbrains.compose.resources.painterResource
import prirodarazumamobile.composeapp.generated.resources.Res
import prirodarazumamobile.composeapp.generated.resources.document
import prirodarazumamobile.composeapp.generated.resources.logout
import prirodarazumamobile.composeapp.generated.resources.main
import prirodarazumamobile.composeapp.generated.resources.nature
import prirodarazumamobile.composeapp.generated.resources.patient
import prirodarazumamobile.composeapp.generated.resources.setting
import prirodarazumamobile.composeapp.generated.resources.user
import prirodarazumamobile.composeapp.generated.resources.users

private val PrimaryColor = Color(0xFF2E7D32)
private val SurfaceColor = Color(0xFFECF5EC)
private val AccentColor = Color(0xFF81C784)
private val TextPrimaryColor = Color(0xFF1B5E20)
private val TextSecondaryColor = Color(0xFF424242)
private val DividerColor = Color(0xFFBDBDBD)

@Composable
fun SideBar(
    roleId: Int,
    onNavigateToUsers: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToRoles: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sidebarWidth = 280.dp

    val sidebarOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-sidebarWidth),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "sidebarOffset"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 0.5f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "overlayAlpha"
    )

    // Добавляем оверлей на весь экран, который будет видим только когда сайдбар открыт
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleVisibility
                )
                .zIndex(5f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(sidebarWidth)
            .offset(x = sidebarOffset)
            .zIndex(10f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp),
                )
                .clip(RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SurfaceColor,
                            SurfaceColor.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.nature),
                                contentDescription = "Логотип",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "«Природа Разума»",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Theme.fonts.nunito,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 0.dp)
                            .size(36.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(AccentColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleVisibility
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Скрыть сайдбар",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Остальной код без изменений
                Divider(
                    color = DividerColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    if (roleId == 1) {
                        NavigationItem(
                            icon = painterResource(Res.drawable.users),
                            label = {
                                Text(
                                    "Специалисты",
                                    fontFamily = Theme.fonts.nunito,
                                    color = TextSecondaryColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            },
                            onClick = {
                                onNavigateToUsers()
                                onToggleVisibility()
                            }
                        )
                    }

                    NavigationItem(
                        icon = painterResource(Res.drawable.patient),
                        label = {
                            Text(
                                "Дети",
                                fontFamily = Theme.fonts.nunito,
                                color = TextSecondaryColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        },
                        onClick = {
                            onNavigateToPatients()
                            onToggleVisibility()
                        }
                    )

                    NavigationItem(
                        icon = painterResource(Res.drawable.document),
                        label = {
                            Text(
                                "Документы",
                                fontFamily = Theme.fonts.nunito,
                                color = TextSecondaryColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        },
                        onClick = {
                            onNavigateToDocuments()
                            onToggleVisibility()
                        }
                    )

                    if (roleId == 1) {
                        NavigationItem(
                            icon = painterResource(Res.drawable.setting),
                            label = {
                                Text(
                                    "Роли",
                                    fontFamily = Theme.fonts.nunito,
                                    color = TextSecondaryColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            },
                            onClick = {
                                onNavigateToRoles()
                                onToggleVisibility()
                            }
                        )
                    }

                    NavigationItem(
                        icon = painterResource(Res.drawable.user),
                        label = {
                            Text(
                                "Личный кабинет",
                                fontFamily = Theme.fonts.nunito,
                                color = TextSecondaryColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        },
                        onClick = {
                            onNavigateToProfile()
                            onToggleVisibility()
                        }
                    )
                }

                Divider(
                    color = DividerColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                NavigationItem(
                    icon = painterResource(Res.drawable.logout),
                    label = {
                        Text(
                            "Выйти",
                            fontFamily = Theme.fonts.nunito,
                            color = TextSecondaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    },
                    onClick = {
                        onLogout()
                        onToggleVisibility()
                    }
                )
            }
        }
    }
}


@Composable
fun NavigationItem(
    icon: Painter,
    label: @Composable () -> Unit,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isHovered) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "backgroundAlpha"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = AccentColor.copy(alpha = backgroundAlpha * 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(if (isHovered) 3.dp else 1.dp, CircleShape)
                    .background(
                        color = if (isHovered) AccentColor else SurfaceColor,
                        shape = CircleShape
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            label()
        }

        if (isHovered) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .height(24.dp)
                    .background(
                        color = PrimaryColor,
                        shape = RoundedCornerShape(0.dp, 4.dp, 4.dp, 0.dp)
                    )
            )
        }
    }
}