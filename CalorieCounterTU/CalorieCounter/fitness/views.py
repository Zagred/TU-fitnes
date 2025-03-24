from django.shortcuts import redirect
from django.urls import reverse
from django.views import generic as views

from CalorieCounter.accounts.models import CustomUser
from CalorieCounter.fitness.models import Trainer, Class


class TrainersView(views.ListView):
    queryset = Trainer.objects.all()
    template_name = 'fitness/trainers.html'


class TrainerDetailsView(views.DetailView):
    model = Trainer
    template_name = 'fitness/trainers_details.html'

    def get_context_data(self, **kwargs):
        context = super(TrainerDetailsView, self).get_context_data(**kwargs)
        user = CustomUser.objects.get(username=self.request.user.username)
        trainer = Trainer.objects.get(pk=self.kwargs['pk'])
        context['is_chosen'] = True if user.trainer_id == trainer else False
        return context


def choose_trainer(request, pk):
    user = CustomUser.objects.get(username=request.user.username)
    trainer = Trainer.objects.get(pk=pk)
    user.trainer_id = trainer
    user.save()
    return redirect(reverse('my trainer', kwargs={'pk': trainer.id}))


def remove_trainer(request, pk):
    user = CustomUser.objects.get(username=request.user.username)
    user.trainer_id = None
    user.save()
    return redirect('trainers')


class ClassesView(views.ListView):
    queryset = Class.objects.all()
    template_name = 'fitness/classes.html'


class MyClassesView(views.ListView):
    template_name = 'fitness/my_classes.html'

    def get_queryset(self):
        return CustomUser.objects.get(username=self.request.user.username).class_id.all()


class ClassDetailsView(views.DetailView):
    model = Class
    template_name = 'fitness/class_details.html'

    def get_context_data(self, **kwargs):
        context = super(ClassDetailsView, self).get_context_data(**kwargs)
        user = CustomUser.objects.get(username=self.request.user.username)
        class_object = Class.objects.get(pk=self.kwargs['pk'])
        context['is_joined'] = user.class_id.filter(id=class_object.id).exists()
        context['users_number'] = len(CustomUser.objects.filter(class_id=self.kwargs['pk']))
        return context


def join_class(request, pk):
    user = CustomUser.objects.get(username=request.user.username)
    class_object = Class.objects.get(pk=pk)
    if class_object.max_user_capacity > len(CustomUser.objects.filter(class_id=pk)):
        user.class_id.add(class_object)
        user.save()
        return redirect('my classes')
    return redirect(reverse('class details', kwargs={'pk': pk}))


def leave_class(request, pk):
    user = CustomUser.objects.get(username=request.user.username)
    class_object = Class.objects.get(pk=pk)
    user.class_id.remove(class_object)
    user.save()
    return redirect('my classes')
