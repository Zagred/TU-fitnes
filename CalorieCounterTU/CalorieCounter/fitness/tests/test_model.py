import datetime

from django.test import TestCase

from CalorieCounter.accounts.models import CustomUser
from CalorieCounter.fitness.models import Class, Trainer


class ClassTest(TestCase):
    ID = 1111
    NAME = 'Test class'
    DATE = datetime.date.today()
    DURATION = 60
    MAX_USER_CAPACITY = 15
    DESCRIPTION = 'Test description'

    def setUp(self) -> None:
        self.trainer = Trainer(
            id=1111,
            first_name='First',
            last_name='Last',
            gender='Male',
            birthday=self.DATE - datetime.timedelta(days=20*365),
            profile_pic_url='https://hips.hearstapps.com/hmg-prod/images/mh-trainer-2-1533576998.png?resize=640:*'
        )

        self.trainer.save()

        self.class_object = Class(
            id=self.ID,
            name=self.NAME,
            duration_in_minutes=self.DURATION,
            date=self.DATE,
            time='12:00:00',
            description=self.DESCRIPTION,
            max_user_capacity=self.MAX_USER_CAPACITY,
            image_url='https://www.brickbodies.com/wp-content/uploads/2023/01/Group-Fitness-Classes-at-the-best-gyms'
                      '-in-Padonia-Lutherville-Timonium-and-Baltimore-MD-1.jpg',
            trainer_id=self.trainer
        )

        self.user = CustomUser(
            id=11111,
            username='user_test',
            password='1234hello',
            gender='Male',
            birthday=self.DATE - datetime.timedelta(days=20*365),
            weight=80,
            height=180
        )

        self.user2 = CustomUser(
            id=11112,
            username='user_test2',
            password='1234hello',
            gender='Male',
            birthday=self.DATE - datetime.timedelta(days=20 * 365),
            weight=80,
            height=180
        )

        self.user.save()
        self.user2.save()

        self.class_object.save()

    def test_user_join__expect_correct_result(self):
        self.user.class_id.add(self.class_object)
        self.assertEqual(len(CustomUser.objects.filter(class_id=self.ID)), 1)

    def test_user_leave__expect_correct_result(self):
        self.user.class_id.add(self.class_object)
        self.user.class_id.remove(self.class_object)
        self.assertEqual(len(CustomUser.objects.filter(class_id=self.ID)), 0)

    def test_two_users_join__expect_correct_result(self):
        self.user.class_id.add(self.class_object)
        self.user2.class_id.add(self.class_object)
        self.assertEqual(len(CustomUser.objects.filter(class_id=self.ID)), 2)

    def test_two_users_one_leave__expect_correct_result(self):
        self.user.class_id.add(self.class_object)
        self.user2.class_id.add(self.class_object)
        self.user2.class_id.remove(self.class_object)
        self.assertEqual(len(CustomUser.objects.filter(class_id=self.ID)), 1)

    def test_trainer_details__expect_correct_result(self):
        self.assertEqual(self.class_object.trainer_id.full_name, 'First Last')
