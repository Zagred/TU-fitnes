from django.core import validators


def validate_name(value):
    if value != value.capitalize() or len(value) < 2:
        raise validators.ValidationError("The name must be capitalized and at least two letters long")
