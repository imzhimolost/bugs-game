package com.games.bugs_game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar)
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
                0 -> "Игрок"
                1 -> "Правила"
                2 -> "Авторы"
                3 -> "Настройки"
                else -> ""
            }
        }.attach()
    }
}

class RegistrationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }
}

class RulesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rules, container, false)
        val tvRules = view.findViewById<TextView>(R.id.tvRules)

        val sampleHtml = """
            <h2>Правила игры «Жуки»</h2>
            <p><b>Цель:</b> нажимать на появляющихся тараканов и зарабатывать очки.</p>
            <p><font color="#D32F2F"><b>Внимание:</b></font> время раунда ограничено!</p>
        """.trimIndent()

        tvRules.text = HtmlCompat.fromHtml(sampleHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        return view
    }
}

class AuthorsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_authors, container, false)
        val lvAuthors = view.findViewById<ListView>(R.id.lvAuthors)

        // Заполняем простой список для предпросмотра
        val authors = arrayOf("Иванов Иван (Разработчик)", "Петрова Анна (Дизайнер)", "Смирнов Сергей (Тестировщик)")
        lvAuthors.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, authors)

        return view
    }
}

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
}