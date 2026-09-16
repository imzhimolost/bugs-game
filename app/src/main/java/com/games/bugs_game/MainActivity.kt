package com.games.bugs_game

import android.os.Bundle
import android.view.View
import android.widget.*

data class Player(
    val name: String,
    val gender: String,
    val course: Int,
    val difficulty: Int,
    val birthDate: String,
    val zodiac: String
)

class MainActivity : androidx.activity.ComponentActivity() {
    private var day = 1
    private var month = 1
    private var year = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val rbMale = findViewById<RadioButton>(R.id.rbMale)
        val spCourse = findViewById<Spinner>(R.id.spCourse)
        val sbDifficulty = findViewById<SeekBar>(R.id.sbDifficulty)
        val calendarView = findViewById<CalendarView>(R.id.cvDate)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val ivZodiac = findViewById<ImageView>(R.id.ivZodiac)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        spCourse.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            courses)

        calendarView.setOnDateChangeListener { _, y, m, d ->
            day = d
            month = m + 1
            year = y
        }

        btnRegister.setOnClickListener {
            val name = etFullName.text.toString().trim()

            if (name.isEmpty()) {
                etFullName.error = "Введите ФИО!"
                return@setOnClickListener
            }

            val gender = if (rbMale.isChecked) "Мужской" else "Женский"

            val course = spCourse.selectedItemPosition + 1
            val difficulty = sbDifficulty.progress + 1
            val date = "$day.$month.$year"

            val zodiacName = getZodiacName(day, month)
            val zodiacImage = getZodiacImage(zodiacName)

            val player = Player(name, gender, course, difficulty, date, zodiacName)

            tvResult.text = """
                Данные игрока:
                • ФИО: ${player.name}
                • Пол: ${player.gender}
                • Курс: ${player.course}
                • Сложность игры: ${player.difficulty} из 5
                • Дата рождения: ${player.birthDate}
                • Знак зодиака: ${player.zodiac}
            """.trimIndent()

            ivZodiac.visibility = View.VISIBLE
            ivZodiac.setImageResource(zodiacImage)
        }
    }

    private fun getZodiacName(day: Int, month: Int): String {
        return when (month) {
            1 -> if (day < 20) "Козерог" else "Водолей"
            2 -> if (day < 19) "Водолей" else "Рыбы"
            3 -> if (day < 21) "Рыбы" else "Овен"
            4 -> if (day < 20) "Овен" else "Телец"
            5 -> if (day < 21) "Телец" else "Близнецы"
            6 -> if (day < 22) "Близнецы" else "Рак"
            7 -> if (day < 23) "Рак" else "Лев"
            8 -> if (day < 23) "Лев" else "Дева"
            9 -> if (day < 23) "Дева" else "Весы"
            10 -> if (day < 23) "Весы" else "Скорпион"
            11 -> if (day < 23) "Скорпион" else "Стрелец"
            12 -> if (day < 22) "Стрелец" else "Козерог"
            else -> "Неизвестно"
        }
    }

    // 6. Подбор картинки под название знака
    private fun getZodiacImage(zodiacName: String): Int {
        return when (zodiacName) {
            "Овен" -> R.drawable.ic_aries
            "Телец" -> R.drawable.ic_taurus
            "Близнецы" -> R.drawable.ic_gemini
            "Рак" -> R.drawable.ic_cancer
            "Лев" -> R.drawable.ic_leo
            "Дева" -> R.drawable.ic_virgo
            "Весы" -> R.drawable.ic_libra
            "Скорпион" -> R.drawable.ic_scorpio
            "Стрелец" -> R.drawable.ic_sagittarius
            "Козерог" -> R.drawable.ic_capricorn
            "Водолей" -> R.drawable.ic_aquarius
            "Рыбы" -> R.drawable.ic_pisces
            else -> android.R.drawable.ic_menu_help
        }
    }
}