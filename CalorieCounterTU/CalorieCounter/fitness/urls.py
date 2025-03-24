from django.urls import path, include

from CalorieCounter.fitness import views


urlpatterns = [
    path('trainers/', views.TrainersView.as_view(), name='trainers'),
    path('trainer-details/<int:pk>/', include([
        path('', views.TrainerDetailsView.as_view(), name='trainer details'),
        path('choose-trainer/', views.choose_trainer, name='choose trainer'),
        path('remove-trainer/', views.remove_trainer, name='remove personal trainer'),
        path('my-trainer/', views.TrainerDetailsView.as_view(), name='my trainer'),
    ])),
    path('classes/', views.ClassesView.as_view(), name='classes'),
    path('class-details/<int:pk>/', include([
        path('', views.ClassDetailsView.as_view(), name='class details'),
        path('join-class/', views.join_class, name='join class'),
        path('leave-class/', views.leave_class, name='leave class'),
    ])),
    path('my-classes/', views.MyClassesView.as_view(), name='my classes'),
]
