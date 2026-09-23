package com.games.bugs_game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 4

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> RegistrationFragment()
                    1 -> RulesFragment()
                    2 -> AuthorsFragment()
                    3 -> SettingsFragment()
                    else -> RegistrationFragment()
                }
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_registration)
                1 -> getString(R.string.tab_rules)
                2 -> getString(R.string.tab_authors)
                3 -> getString(R.string.tab_settings)
                else -> ""
            }
        }.attach()
    }
}

class RegistrationFragment : Fragment() {
    private var day = 1
    private var month = 1
    private var year = 2000

    //регистрация
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val rbMale = view.findViewById<RadioButton>(R.id.rbMale)
        val spCourse = view.findViewById<Spinner>(R.id.spCourse)
        val sbDifficulty = view.findViewById<SeekBar>(R.id.sbDifficulty)
        val calendarView = view.findViewById<CalendarView>(R.id.cvDate)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val ivZodiac = view.findViewById<ImageView>(R.id.ivZodiac)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс", "5 курс")
        spCourse.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, courses)

        calendarView.setOnDateChangeListener { _, y, m, d ->
            day = d; month = m + 1; year = y
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
            val zodiac = getZodiac(day, month)

            tvResult.text = "Игрок: $name\nПол: $gender\nКурс: $course\nСложность: $difficulty\nДата: $date\nЗнак: ${zodiac.first}"
            ivZodiac.visibility = View.VISIBLE
            ivZodiac.setImageResource(zodiac.second)
        }

        return view
    }

    private fun getZodiac(d: Int, m: Int): Pair<String, Int> {
        return when (m) {
            1 -> if (d < 20) Pair("Козерог", R.drawable.ic_capricorn) else Pair("Водолей", R.drawable.ic_aquarius)
            2 -> if (d < 19) Pair("Водолей", R.drawable.ic_aquarius) else Pair("Рыбы", R.drawable.ic_pisces)
            3 -> if (d < 21) Pair("Рыбы", R.drawable.ic_pisces) else Pair("Овен", R.drawable.ic_aries)
            4 -> if (d < 20) Pair("Овен", R.drawable.ic_aries) else Pair("Телец", R.drawable.ic_taurus)
            5 -> if (d < 21) Pair("Телец", R.drawable.ic_taurus) else Pair("Близнецы", R.drawable.ic_gemini)
            6 -> if (d < 22) Pair("Близнецы", R.drawable.ic_gemini) else Pair("Рак", R.drawable.ic_cancer)
            7 -> if (d < 23) Pair("Рак", R.drawable.ic_cancer) else Pair("Лев", R.drawable.ic_leo)
            8 -> if (d < 23) Pair("Лев", R.drawable.ic_leo) else Pair("Дева", R.drawable.ic_virgo)
            9 -> if (d < 23) Pair("Дева", R.drawable.ic_virgo) else Pair("Весы", R.drawable.ic_libra)
            10 -> if (d < 23) Pair("Весы", R.drawable.ic_libra) else Pair("Скорпион", R.drawable.ic_scorpio)
            11 -> if (d < 23) Pair("Скорпион", R.drawable.ic_scorpio) else Pair("Стрелец", R.drawable.ic_sagittarius)
            12 -> if (d < 22) Pair("Стрелец", R.drawable.ic_sagittarius) else Pair("Козерог", R.drawable.ic_capricorn)
            else -> Pair("Неизвестно", android.R.drawable.ic_menu_help)
        }
    }
}

//правила
class RulesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rules, container, false)
        val tvRules = view.findViewById<TextView>(R.id.tvRules)

        val htmlRules = getString(R.string.game_rules_html)
        tvRules.text = HtmlCompat.fromHtml(htmlRules, HtmlCompat.FROM_HTML_MODE_LEGACY)

        return view
    }
}
//авторы
class AuthorsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_authors, container, false)
        val lvAuthors = view.findViewById<ListView>(R.id.lvAuthors)

        val authorsList = listOf(
            Author("Шинкаренко Ксения", R.drawable.ph_author),
        )

        lvAuthors.adapter = AuthorAdapter(requireContext(), authorsList)

        return view
    }
}

//настройки
class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val sbSpeed = view.findViewById<SeekBar>(R.id.sbSpeed)
        val tvSpeedLabel = view.findViewById<TextView>(R.id.tvSpeedLabel)

        val sbMaxBugs = view.findViewById<SeekBar>(R.id.sbMaxBugs)
        val tvMaxBugsLabel = view.findViewById<TextView>(R.id.tvMaxBugsLabel)

        val sbBonusInterval = view.findViewById<SeekBar>(R.id.sbBonusInterval)
        val tvBonusIntervalLabel = view.findViewById<TextView>(R.id.tvBonusIntervalLabel)

        val sbRoundDuration = view.findViewById<SeekBar>(R.id.sbRoundDuration)
        val tvRoundDurationLabel = view.findViewById<TextView>(R.id.tvRoundDurationLabel)

        sbSpeed.setOnSeekBarChangeListener(simpleSeekBarListener { progress ->
            tvSpeedLabel.text = "Скорость игры: ${progress + 1}x"
        })

        sbMaxBugs.setOnSeekBarChangeListener(simpleSeekBarListener { progress ->
            tvMaxBugsLabel.text = "Макс. тараканов на экране: $progress"
        })

        sbBonusInterval.setOnSeekBarChangeListener(simpleSeekBarListener { progress ->
            tvBonusIntervalLabel.text = "Интервал появления бонусов: $progress сек."
        })

        sbRoundDuration.setOnSeekBarChangeListener(simpleSeekBarListener { progress ->
            tvRoundDurationLabel.text = "Длительность раунда: $progress сек."
        })

        return view
    }

    private fun simpleSeekBarListener(onProgress: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) = onProgress(progress)
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        }
    }
}