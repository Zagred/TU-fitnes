package com.example.myapplication.datamanager.quotes

object QuotesManager {

        private val motivationalQuotes = arrayOf(
            "Yesterday, you said I will do it tomorrow!",
            "Discipline > Motivation",
            "Do it tired. Do it scared. Just do it.",
            "You'll never feel like it. Do it anyway.",
            "No zero days",
            "A year from now, you'll wish you started today.",
            "One day or day one. You decide.",
            "Motivation fades. Habits stick.",
            "Progress over perfection.",
            "Get comfortable being uncomfortable",
            "Nothing changes if nothing changes.",
            "The hardest part is starting. Begin now.",
            "Success isn't owned. It's leased, and rent is due every day.",
            "Focus on the process, not the outcome.",
            "Small steps every day lead to massive results.",
            "Fall seven times, stand up eight.",
            "Your body can stand almost anything. It's your mind you have to convince.",
            "Doubting yourself is normal. Giving up is optional.",
            "Dedication sees dreams come true.",
            "The only bad workout is the one that didn't happen."
        )

        fun getRandomQuote(): String {
            return motivationalQuotes.random()
        }
    }
