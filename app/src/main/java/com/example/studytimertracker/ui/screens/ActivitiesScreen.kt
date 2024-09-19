package com.example.studytimertracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.example.studytimertracker.model.Activity
import com.example.studytimertracker.model.ActivityType
import com.example.studytimertracker.ui.components.ActivityRow
import com.example.studytimertracker.ui.components.AddActivityDialog
import com.example.studytimertracker.ui.components.UpdateDeleteActivityDialog
import com.example.studytimertracker.viewmodel.ActivitiesViewModel

@Composable
fun ActivitiesScreen(viewModel: ActivitiesViewModel) {
    val activities by viewModel.activities.observeAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<Activity?>(null) }

    // Separate work and rest activities
    val workActivities = activities.filter { it.type == ActivityType.WORK }
    val restActivities = activities.filter { it.type == ActivityType.REST }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Work Activities Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(min = 150.dp, max = 250.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Work Activities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (workActivities.isEmpty()) {
                    Text(
                        text = "No work activities available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val workListState = rememberLazyListState()
                        LazyColumn(
                            state = workListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 200.dp)
                        ) {
                            items(workActivities) { activity ->
                                ActivityRow(activity = activity,
                                    onClick = { activityToEdit = activity}
                                )
                            }
                        }

                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            scrollState = workListState
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rest Activities Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(min = 150.dp, max = 250.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Rest Activities",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                )

                if (restActivities.isEmpty()) {
                    Text(
                        text = "No rest activities available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val restListState = rememberLazyListState()
                        LazyColumn(
                            state = restListState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 200.dp)
                        ) {
                            items(restActivities) { activity ->
                                ActivityRow(
                                    activity = activity,
                                    onClick = { activityToEdit = activity }
                                    )
                            }
                        }

                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            scrollState = restListState
                        )
                    }
                }
            }
        }

        Text(
            text = "Tap on an activity to edit it",
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Add Activity Button
        Button(onClick = { showAddDialog = true }) {
            Text("Add Activity")
        }

        // Dialog to add activity
        if (showAddDialog) {
            AddActivityDialog(onDismiss = { showAddDialog = false }) { name, multiplier, type ->
                viewModel.addActivity(name, multiplier, type) // Add the activity through the ViewModel
                showAddDialog = false
            }
        }

        // Dialog to update/delete activity
        activityToEdit?.let { activity ->
            UpdateDeleteActivityDialog(
                activity = activity,
                onDismiss = { activityToEdit = null },
                onUpdate = { name, multiplier, type ->
                    viewModel.updateActivity(activity.copy(name = name, multiplier = multiplier, type = type))
                    activityToEdit = null
                },
                onDelete = {
                    viewModel.deleteActivity(activity)
                    activityToEdit = null
                }
            )
        }
    }
}

@Composable
fun VerticalScrollbar(modifier: Modifier = Modifier, scrollState: LazyListState) {
    val itemsVisible = scrollState.layoutInfo.visibleItemsInfo.size
    val totalItems = scrollState.layoutInfo.totalItemsCount
    if (totalItems > itemsVisible) {
        val scrollProgress = (scrollState.firstVisibleItemIndex.toFloat() / (totalItems - itemsVisible))
        val scrollbarHeight = itemsVisible.toFloat() / totalItems

        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(scrollbarHeight)
                    .align(Alignment.TopStart)
                    .offset(y = scrollProgress * (1f - scrollbarHeight) * 100.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
