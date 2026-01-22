package com.example.gcinemon

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONArray
import org.json.JSONObject

class PresetSettingActivity : AppCompatActivity() {

    private enum class ChipColor { ORANGE, GREEN, BLUE }

    private data class PresetItem(
        val id: Long,
        val label: String,
        val color: ChipColor
    )

    private lateinit var ivBack: ImageView

    private lateinit var chipGroupSavedPosition: ChipGroup
    private lateinit var chipGroupSavedWorkType: ChipGroup

    private lateinit var cardAddPosition: MaterialCardView
    private lateinit var cardAddWorkType: MaterialCardView

    private lateinit var etAddPosition: EditText
    private lateinit var etAddWorkType: EditText
    private lateinit var ivAddPosition: ImageView
    private lateinit var ivAddWorkType: ImageView

    private val addColorCycle = listOf(ChipColor.ORANGE, ChipColor.GREEN, ChipColor.BLUE)
    private var positionColorCursor = 0
    private var workTypeColorCursor = 0

    private val savedPositions = mutableListOf<PresetItem>()
    private val savedWorkTypes = mutableListOf<PresetItem>()

    private val prefs by lazy { getSharedPreferences("preset_prefs", MODE_PRIVATE) }
    private val KEY_POSITIONS = "positions"
    private val KEY_WORKTYPES = "worktypes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preset_setting)

        ivBack = findViewById(R.id.ivBack)

        chipGroupSavedPosition = findViewById(R.id.chipGroupSavedPosition)
        chipGroupSavedWorkType = findViewById(R.id.chipGroupSavedWorkType)

        cardAddPosition = findViewById(R.id.cardAddPosition)
        cardAddWorkType = findViewById(R.id.cardAddWorkType)

        etAddPosition = findViewById(R.id.etAddPosition)
        etAddWorkType = findViewById(R.id.etAddWorkType)
        ivAddPosition = findViewById(R.id.ivAddPosition)
        ivAddWorkType = findViewById(R.id.ivAddWorkType)

        ivBack.setOnClickListener { finish() }

        chipGroupSavedPosition.removeAllViews()
        chipGroupSavedWorkType.removeAllViews()

        bindInputBorder(etAddPosition, cardAddPosition)
        bindInputBorder(etAddWorkType, cardAddWorkType)

        loadFromPrefs()
        renderAll()

        ivAddPosition.setOnClickListener {
            val text = etAddPosition.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                val color = nextPositionColor()
                val item = PresetItem(System.currentTimeMillis(), text, color)
                savedPositions.add(item)
                addChip(chipGroupSavedPosition, item) {
                    savedPositions.removeAll { it.id == item.id }
                    saveToPrefs()
                }
                saveToPrefs()
                etAddPosition.setText("")
            }
        }

        ivAddWorkType.setOnClickListener {
            val text = etAddWorkType.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                val color = nextWorkTypeColor()
                val item = PresetItem(System.currentTimeMillis(), text, color)
                savedWorkTypes.add(item)
                addChip(chipGroupSavedWorkType, item) {
                    savedWorkTypes.removeAll { it.id == item.id }
                    saveToPrefs()
                }
                saveToPrefs()
                etAddWorkType.setText("")
            }
        }
    }

    private fun nextPositionColor(): ChipColor {
        val c = addColorCycle[positionColorCursor % addColorCycle.size]
        positionColorCursor++
        return c
    }

    private fun nextWorkTypeColor(): ChipColor {
        val c = addColorCycle[workTypeColorCursor % addColorCycle.size]
        workTypeColorCursor++
        return c
    }

    private fun bindInputBorder(et: EditText, card: MaterialCardView) {
        fun apply() {
            val active = et.hasFocus() || !et.text.isNullOrBlank()
            card.strokeColor = ContextCompat.getColor(
                this,
                if (active) R.color.gc_orange_main else R.color.gc_black_40
            )
        }

        et.setOnFocusChangeListener { _, _ -> apply() }
        et.addTextChangedListener { apply() }
        apply()
    }

    private fun renderAll() {
        chipGroupSavedPosition.removeAllViews()
        chipGroupSavedWorkType.removeAllViews()

        for (item in savedPositions) {
            addChip(chipGroupSavedPosition, item) {
                savedPositions.removeAll { it.id == item.id }
                saveToPrefs()
            }
        }

        for (item in savedWorkTypes) {
            addChip(chipGroupSavedWorkType, item) {
                savedWorkTypes.removeAll { it.id == item.id }
                saveToPrefs()
            }
        }
    }

    private fun addChip(group: ChipGroup, item: PresetItem, onRemoved: () -> Unit) {
        val chip = Chip(this).apply {
            text = item.label
            isClickable = false
            isCheckable = false

            setEnsureMinTouchTargetSize(false)

            setTextColor(ContextCompat.getColor(context, R.color.gc_black_70))
            textSize = 14f

            setChipMinHeight(dp(26f))
            setChipCornerRadius(dp(16f))

            chipStrokeWidth = dp(1f)
            chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, strokeColorRes(item.color)))
            chipBackgroundColor = ColorStateList.valueOf(bgColorInt(item.color))

            setChipStartPadding(dp(18f))
            setChipEndPadding(dp(12f))

            isCloseIconVisible = true
            closeIcon = ContextCompat.getDrawable(context, R.drawable.ic_bs_close_8)
            closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gc_black_50))
            closeIconSize = dp(8f)
            setCloseIconStartPadding(dp(8f))
            setCloseIconEndPadding(0f)

            setOnCloseIconClickListener {
                group.removeView(this)
                onRemoved()
            }
        }

        group.addView(chip)
    }

    private fun strokeColorRes(c: ChipColor): Int = when (c) {
        ChipColor.ORANGE -> R.color.gc_orange_main
        ChipColor.GREEN -> R.color.gc_green_sub
        ChipColor.BLUE -> R.color.gc_blue_public
    }

    private fun bgColorInt(c: ChipColor): Int = when (c) {
        ChipColor.ORANGE -> Color.parseColor("#33FE6301")
        ChipColor.GREEN -> Color.parseColor("#3306AF00")
        ChipColor.BLUE -> Color.parseColor("#330073FF")
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }

    private fun saveToPrefs() {
        prefs.edit()
            .putString(KEY_POSITIONS, toJson(savedPositions))
            .putString(KEY_WORKTYPES, toJson(savedWorkTypes))
            .apply()
    }

    private fun loadFromPrefs() {
        savedPositions.clear()
        savedWorkTypes.clear()

        val posStr = prefs.getString(KEY_POSITIONS, null)
        val workStr = prefs.getString(KEY_WORKTYPES, null)

        if (!posStr.isNullOrBlank()) savedPositions.addAll(fromJson(posStr))
        if (!workStr.isNullOrBlank()) savedWorkTypes.addAll(fromJson(workStr))
    }

    private fun toJson(list: List<PresetItem>): String {
        val arr = JSONArray()
        for (item in list) {
            val o = JSONObject()
            o.put("id", item.id)
            o.put("label", item.label)
            o.put("color", item.color.name)
            arr.put(o)
        }
        return arr.toString()
    }

    private fun fromJson(json: String): List<PresetItem> {
        val result = mutableListOf<PresetItem>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getLong("id")
            val label = o.getString("label")
            val color = ChipColor.valueOf(o.getString("color"))
            result.add(PresetItem(id, label, color))
        }
        return result
    }
}
