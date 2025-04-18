import datetime

from django.contrib.auth.models import AbstractUser
from django.core import validators
from django.db import models

from CalorieCounter.accounts.validators import validate_age_through_birthday
from CalorieCounter.fitness.models import Trainer, Class


class CustomUser(AbstractUser):
    MAX_GENDER_LEN = 6
    MIN_HEIGHT = 60
    MAX_HEIGHT = 250
    MIN_WEIGHT = 30

    GENDER_CHOICES = [
        ('Male', 'Male'),
        ('Female', 'Female'),
    ]

    birthday = models.DateField(
        validators=(
            validate_age_through_birthday,
        )
    )

    gender = models.CharField(
        max_length=MAX_GENDER_LEN,
        choices=GENDER_CHOICES,
    )

    height = models.PositiveIntegerField(
        validators=(
            validators.MinValueValidator(MIN_HEIGHT),
            validators.MaxValueValidator(MAX_HEIGHT)
        ),
        error_messages={
            'min_value': 'Please enter a valid height!',
            'max_value': 'Please enter a valid height!',
        },
    )

    weight = models.FloatField(
        validators=(
            validators.MinValueValidator(MIN_WEIGHT),
        ),
        error_messages={
            'min_value': 'Please enter a valid weight!'
        }
    )

    profile_image = models.URLField(
        blank=True,
        null=True,
    )

    motivations_group = models.BooleanField(
        default=False,
    )

    calories_per_day = models.PositiveIntegerField(
        blank=True,
        null=True,
    )

    fats_grams_per_day = models.PositiveIntegerField(
        blank=True,
        null=True,
    )

    proteins_grams_per_day = models.PositiveIntegerField(
        blank=True,
        null=True,
    )

    carbs_grams_per_day = models.PositiveIntegerField(
        blank=True,
        null=True,
    )

    REQUIRED_FIELDS = ['birthday', 'gender', 'height', 'weight']

    trainer_id = models.ForeignKey(
        to=Trainer,
        on_delete=models.CASCADE,
        blank=True,
        null=True,
    )

    class_id = models.ManyToManyField(
        to=Class,
        blank=True,
    )

    @property
    def age(self):
        today = datetime.date.today()
        age = today.year - self.birthday.year
        if self.birthday.month > today.month or self.birthday.month == today.month and self.birthday.day > today.day:
            age -= 1
        return age

    def __str__(self):
        return self.username

    class Meta:
        ordering = ('username',)
