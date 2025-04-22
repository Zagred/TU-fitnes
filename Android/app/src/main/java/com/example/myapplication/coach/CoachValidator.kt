package com.example.myapplication.datamanager.coach

class CoachValidator {

    companion object {
        private const val MIN_NAME_LENGTH = 2
        private const val MAX_NAME_LENGTH = 50
        private const val MIN_CONTACT_INFO_LENGTH = 5
        private const val MAX_CONTACT_INFO_LENGTH = 100

        val VALID_SPECIALIZATIONS = listOf(
            "Strength Training",
            "Cardio",
            "Yoga",
            "Pilates",
            "Nutrition",
            "Weight Loss",
            "Body Building",
            "CrossFit",
            "Rehabilitation",
            "Sports Performance",
            "Functional Training",
            "General Fitness"
        )

        private val EMAIL_PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")

        private val PHONE_PATTERN = Regex("^[+]?[0-9]{10,15}$")
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Name cannot be empty")
            name.length < MIN_NAME_LENGTH -> ValidationResult(false, "Name must be at least $MIN_NAME_LENGTH characters")
            name.length > MAX_NAME_LENGTH -> ValidationResult(false, "Name cannot exceed $MAX_NAME_LENGTH characters")
            !isValidCharacters(name) -> ValidationResult(false, "Name contains invalid characters")
            else -> ValidationResult(true)
        }
    }

    fun validateSpecialization(specialization: String): ValidationResult {
        return when {
            specialization.isBlank() -> ValidationResult(false, "Specialization cannot be empty")
            !VALID_SPECIALIZATIONS.contains(specialization) -> ValidationResult(false, "Please select a valid specialization")
            else -> ValidationResult(true)
        }
    }

    fun validateContactInfo(contactInfo: String): ValidationResult {
        return when {
            contactInfo.isBlank() -> ValidationResult(false, "Contact information cannot be empty")
            contactInfo.length < MIN_CONTACT_INFO_LENGTH -> ValidationResult(false, "Contact information must be at least $MIN_CONTACT_INFO_LENGTH characters")
            contactInfo.length > MAX_CONTACT_INFO_LENGTH -> ValidationResult(false, "Contact information cannot exceed $MAX_CONTACT_INFO_LENGTH characters")
            !isValidEmail(contactInfo) && !isValidPhone(contactInfo) -> ValidationResult(false, "Contact information must be a valid email or phone number")
            else -> ValidationResult(true)
        }
    }

    fun validateCoach(coach: Coach): ValidationResult {
        val nameValidation = validateName(coach.name)
        if (!nameValidation.isValid) return nameValidation

        val specializationValidation = validateSpecialization(coach.specialization)
        if (!specializationValidation.isValid) return specializationValidation

        return validateContactInfo(coach.contactInfo)
    }

    private fun isValidCharacters(text: String): Boolean {
        return text.all { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }
    }

    private fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matches(email)
    }

    private fun isValidPhone(phone: String): Boolean {
        return PHONE_PATTERN.matches(phone)
    }

    suspend fun checkDuplicate(coachDAO: CoachDAO, name: String, contactInfo: String): ValidationResult {
        val coaches = coachDAO.getAllActiveCoaches()
        val isDuplicate = coaches.any {
            it.name.equals(name, ignoreCase = true) ||
                    it.contactInfo.equals(contactInfo, ignoreCase = true)
        }

        return if (isDuplicate) {
            ValidationResult(false, "A coach with this name or contact info already exists")
        } else {
            ValidationResult(true)
        }
    }
}