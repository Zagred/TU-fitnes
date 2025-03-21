from django.test import TestCase

from CalorieCounter.accounts.models import CustomUser
from CalorieCounter.calorie_counter.models import DailyData
from CalorieCounter.core.models import Meal, FoodPlan, Food, Exercise


class DailyDataTest(TestCase):
    ID = 999999

    def setUp(self) -> None:
        self.user = CustomUser(
            id=11111,
            username='user_test',
            password='1234hello',
            gender='Male',
            birthday='2000-01-01',
            weight=80,
            height=180
        )

        self.user.save()

        self.daily_data = DailyData(
            id=self.ID,
            user_id=self.user,
            total_calories=self.user.calories_per_day,
            calories_burnt=0,
            calories_eaten=0,
            fats_grams_per_day=self.user.fats_grams_per_day,
            proteins_grams_per_day=self.user.proteins_grams_per_day,
            carbs_grams_per_day=self.user.carbs_grams_per_day
        )

        self.plan = FoodPlan(
            id=192,
            name='Plan',
            image_url='https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.healthline.com',
        )

        self.meal = Meal(
            id=422,
            name='Meal',
            calories=412,
            grams=254,
            fats_grams=32,
            protein_grams=25,
            carbs_grams=65
        )

        self.food = Food(
            id=999,
            name='Food',
            calories_per_100g=300,
            protein_per_100g=40,
            carbs_per_100g=30,
            fats_per_100g=30
        )

        self.exercise = Exercise(
            id=999,
            name='Exercise',
            metabolic_equivalent=10
        )

        self.food.save()
        self.exercise.save()
        self.meal.save()
        self.plan.save()

        self.plan.meals.add(self.meal)
        self.plan.save()

        self.daily_data.save()

    def test_calories_for_the_day__expect_correct_result(self):
        self.assertEqual(self.daily_data.total_calories, self.user.calories_per_day)

    def test_consumed_calories_after_adding_food__expect_correct_result(self):
        self.food.daily_data_pk.add(self.daily_data)
        objects = Food.objects.filter(daily_data_pk=self.ID)
        calories = sum(map(lambda x: x.calories, objects))
        self.assertEqual(calories, self.food.calories)

    def test_consumed_calories_after_adding_meal__expect_correct_result(self):
        self.meal.daily_data_pk.add(self.daily_data)
        objects = Meal.objects.filter(daily_data_pk=self.ID)
        calories = sum(map(lambda x: x.calories, objects))
        self.assertEqual(calories, self.meal.calories)

    def test_consumed_carbs_after_adding_meal__expect_correct_result(self):
        self.meal.daily_data_pk.add(self.daily_data)
        objects = Meal.objects.filter(daily_data_pk=self.ID)
        carbs = sum(map(lambda x: x.carbs_grams, objects))
        self.assertEqual(carbs, self.meal.carbs_grams)

    def test_consumed_calories_after_adding_food_plan__expect_correct_result(self):
        self.plan.daily_data_pk.add(self.daily_data)
        objects = FoodPlan.objects.filter(daily_data_pk=self.ID)
        calories = sum(map(lambda x: x.calories, objects))
        self.assertEqual(calories, self.plan.calories)

    def test_consumed_fats_after_adding_food_plan__expect_correct_result(self):
        self.plan.daily_data_pk.add(self.daily_data)
        objects = FoodPlan.objects.filter(daily_data_pk=self.ID)
        fats = sum(map(lambda x: x.fats_grams, objects))
        self.assertEqual(fats, self.plan.fats_grams)

    def test_consumed_protein_after_adding_food_plan__expect_correct_result(self):
        self.plan.daily_data_pk.add(self.daily_data)
        objects = FoodPlan.objects.filter(daily_data_pk=self.ID)
        proteins = sum(map(lambda x: x.protein_grams, objects))
        self.assertEqual(proteins, self.plan.protein_grams)

    def test_burnt_calories_after_adding_exercise__expect_correct_result(self):
        self.exercise.daily_data_pk.add(self.daily_data)
        objects = Exercise.objects.filter(daily_data_pk=self.ID)
        calories = sum(map(lambda x: x.metabolic_equivalent * 10, objects))
        self.assertEqual(calories, self.exercise.metabolic_equivalent * 10)
