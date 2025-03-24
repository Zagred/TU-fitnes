import datetime

from django.core import validators


def validate_name(value):
    if value != value.capitalize() or len(value) < 2:
        raise validators.ValidationError("The name must be capitalized and at least two letters long")


def validate_date(value):
    if value < datetime.date.today():
        raise validators.ValidationError("The date must be in the future")
