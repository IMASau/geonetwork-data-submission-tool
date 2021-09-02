from allauth.account.views import PasswordChangeView
from django.contrib import admin
from django.contrib.auth.decorators import login_required
from django.urls import path, include, reverse_lazy
from django.views.generic import TemplateView


class CustomPasswordChangeView(PasswordChangeView):
    success_url = reverse_lazy("account_profile")


custom_password_change = login_required(CustomPasswordChangeView.as_view())

urlpatterns = [
    path('', include('metcalf.imas.frontend.urls')),
    path('accounts/profile/', TemplateView.as_view(template_name='account/profile.html'), name="account_profile"),
    path('accounts/password/change/', custom_password_change),
    path('accounts/', include('allauth.urls')),
    path('admin/', admin.site.urls),
]
