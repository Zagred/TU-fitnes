from django.contrib import admin

from .models import Trainer, Class


@admin.register(Trainer)
class TrainerAdmin(admin.ModelAdmin):
    model = Trainer
    list_display = ('first_name', 'last_name')


@admin.register(Class)
class ClassAdmin(admin.ModelAdmin):
    model = Class
    list_display = ('name', 'date', 'time',)
