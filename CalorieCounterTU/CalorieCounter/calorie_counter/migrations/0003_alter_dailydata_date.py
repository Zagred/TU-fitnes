# Generated by Django 4.1.3 on 2025-03-18 08:57

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('calorie_counter', '0002_alter_dailydata_date'),
    ]

    operations = [
        migrations.AlterField(
            model_name='dailydata',
            name='date',
            field=models.DateField(auto_now_add=True),
        ),
    ]
