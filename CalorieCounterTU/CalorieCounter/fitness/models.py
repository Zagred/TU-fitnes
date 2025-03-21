import datetime

from django.core import validators
from django.db import models

from CalorieCounter.accounts.validators import validate_age_through_birthday
from CalorieCounter.fitness.validators import validate_name, validate_date


class Trainer(models.Model):
    MAX_NAME_LEN = 50
    MAX_GENDER_LEN = 6
    MAX_BIO_LEN = 250

    MAX_NAME_ERROR_MSG = "Maximum name length is 50 characters!"
    MAX_BIO_ERROR_MSG = "Maximum length of your biography is 250 characters!"
    INVALID_URL_MSG = "Invalid URL!"

    GENDER_CHOICES = [
        ('Male', 'Male'),
        ('Female', 'Female'),
    ]

    first_name = models.CharField(
        max_length=MAX_NAME_LEN,
        validators=[validate_name],
        error_messages={
            'max_length': MAX_NAME_ERROR_MSG
        }
    )

    last_name = models.CharField(
        max_length=MAX_NAME_LEN,
        validators=[validate_name],
        error_messages={
            'max_length': MAX_NAME_ERROR_MSG,
        }
    )

    gender = models.CharField(
        max_length=MAX_GENDER_LEN,
        choices=GENDER_CHOICES,
    )

    birthday = models.DateField(
        validators=(
            validate_age_through_birthday,
        )
    )

    biography = models.TextField(
        blank=True,
        null=True,
        max_length=MAX_BIO_LEN,
        error_messages={
            'max_length': MAX_BIO_ERROR_MSG,
        }
    )

    profile_pic_url = models.URLField(
        validators=[validators.URLValidator()],
        error_messages={
            'url': INVALID_URL_MSG,
        }
    )

    @property
    def age(self):
        age = datetime.date.today().year - self.birthday.year
        if (self.birthday.month > datetime.date.today().month or self.birthday.month == datetime.date.today().month
                and self.birthday.day > datetime.date.today().day):
            age -= 1
        return age

    @property
    def full_name(self):
        return f'{self.first_name} {self.last_name}'

    class Meta:
        ordering = ('first_name', 'last_name',)


class Class(models.Model):
    MAX_NAME_LEN = 50
    MIN_DURATION_IN_MINUTES = 10
    MAX_DESCRIPTION_LEN = 250
    MIN_USER_CAPACITY = 5

    DURATION_IN_MIN_ERROR_MSG = f'Duration must be at least {MIN_DURATION_IN_MINUTES} seconds.'
    INVALID_URL_MSG = "Invalid URL!"

    name = models.CharField(
        max_length=MAX_NAME_LEN,
    )

    duration_in_minutes = models.IntegerField(
        validators=[validators.MinValueValidator(MIN_DURATION_IN_MINUTES),],
        error_messages={
            'min_value': DURATION_IN_MIN_ERROR_MSG
        }
    )

    time = models.TimeField()

    date = models.DateField(
        validators=[validate_date]
    )

    description = models.TextField(
        max_length=MAX_DESCRIPTION_LEN,
    )

    max_user_capacity = models.IntegerField(
        validators=[validators.MinValueValidator(MIN_USER_CAPACITY)]
    )

    image_url = models.URLField(
        validators=[validators.URLValidator()],
        error_messages={
            'url': INVALID_URL_MSG,
        }
    )

    trainer_id = models.ForeignKey(
        to=Trainer,
        on_delete=models.CASCADE,
        blank=True,
        null=True,
    )

    class Meta:
        ordering = ('name',)
