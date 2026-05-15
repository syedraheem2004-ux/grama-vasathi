package com.grama.vasathi.ui.host

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.grama.vasathi.data.local.AppDatabase
import com.grama.vasathi.data.local.ChecklistItemEntity
import com.grama.vasathi.data.model.ChecklistItem
import com.grama.vasathi.data.model.ChecklistSection
import com.grama.vasathi.data.model.ReadinessStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {
    private val db by lazy { AppDatabase.getDatabase(application) }
    private val dao by lazy { db.checklistDao() }
    private val database = FirebaseDatabase.getInstance()

    private val _sections = MutableStateFlow<List<ChecklistSection>>(emptyList())
    val sections = _sections.asStateFlow()

    private val _overallProgress = MutableStateFlow(0f)
    val overallProgress = _overallProgress.asStateFlow()

    private val _readinessStatus = MutableStateFlow(ReadinessStatus.NOT_READY)
    val readinessStatus = _readinessStatus.asStateFlow()

    init {
        observeChecklist()
    }

    private fun observeChecklist() {
        viewModelScope.launch {
            try {
                val currentItems = withContext(Dispatchers.IO) {
                    dao.getAllItems().first()
                }
                if (currentItems.isEmpty()) {
                    seedInitialData()
                }
                
                dao.getAllItems().collect { entities ->
                    processEntities(entities)
                }
            } catch (e: Exception) {
                // Handle potential DB initialization errors
            }
        }
    }

    private fun seedInitialData() {
        val initialItems = listOf(
            // Room & Bedding
            ChecklistItemEntity("rb1", "s1", "Clean bed linen provided", false),
            ChecklistItemEntity("rb2", "s1", "Pillow and blanket available", false),
            ChecklistItemEntity("rb3", "s1", "Mosquito net installed", false),
            ChecklistItemEntity("rb4", "s1", "Room is well ventilated", false),
            // Hygiene
            ChecklistItemEntity("hs1", "s2", "Attached or nearby clean bathroom", false),
            ChecklistItemEntity("hs2", "s2", "Soap and toiletries provided", false),
            ChecklistItemEntity("hs3", "s2", "Dustbin present in room", false),
            ChecklistItemEntity("hs4", "s2", "Floor cleaned daily", false),
            // Water & Food
            ChecklistItemEntity("wf1", "s3", "Safe drinking water available", false),
            ChecklistItemEntity("wf2", "s3", "Home-cooked meals offered", false),
            ChecklistItemEntity("wf3", "s3", "Kitchen hygiene maintained", false),
            // Safety
            ChecklistItemEntity("sa1", "s4", "Room door has a working lock", false),
            ChecklistItemEntity("sa2", "s4", "Emergency contact number posted", false),
            ChecklistItemEntity("sa3", "s4", "Well-lit pathway to room at night", false),
            // Connectivity
            ChecklistItemEntity("cc1", "s5", "Mobile network available", false),
            ChecklistItemEntity("cc2", "s5", "Charging point in room", false),
            ChecklistItemEntity("cc3", "s5", "Fan or ventilation provided", false)
        )
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertItems(initialItems)
        }
    }

    private fun processEntities(entities: List<ChecklistItemEntity>) {
        try {
            val mappedItems = entities.map { ChecklistItem(it.itemId, it.label, it.isChecked, it.proofImageUrl) }
            
            val newSections = listOf(
                ChecklistSection("s1", "Room & Bedding", "🛏️", mappedItems.filter { it.itemId.startsWith("rb") }),
                ChecklistSection("s2", "Hygiene & Sanitation", "🚿", mappedItems.filter { it.itemId.startsWith("hs") }),
                ChecklistSection("s3", "Water & Food", "💧", mappedItems.filter { it.itemId.startsWith("wf") }),
                ChecklistSection("s4", "Safety & Access", "🔒", mappedItems.filter { it.itemId.startsWith("sa") }),
                ChecklistSection("s5", "Connectivity & Comfort", "📶", mappedItems.filter { it.itemId.startsWith("cc") })
            )
            
            _sections.value = newSections
            
            val total = entities.size
            val checked = entities.count { it.isChecked }
            val progress = if (total > 0) checked.toFloat() / total else 0f
            _overallProgress.value = progress
            
            _readinessStatus.value = when {
                progress >= 1.0f -> ReadinessStatus.GUEST_READY
                progress >= 0.6f -> ReadinessStatus.ALMOST_READY
                else -> ReadinessStatus.NOT_READY
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    fun toggleItem(itemId: String, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dao.updateItemStatus(itemId, isChecked)
                saveToFirebase()
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private fun saveToFirebase() {
        // Simulating firebase sync
    }

    fun submitChecklist() {
        // Logic to activate listing
    }
}
