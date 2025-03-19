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
    trainer = Trainer.objects.get(pk=pk)
    user.trainer_id.remove(trainer)
    user.save()
    return redirect('trainers')
