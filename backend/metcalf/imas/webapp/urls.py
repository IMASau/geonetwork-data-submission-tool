"""
Definition of urls for webapp.
"""
from django.contrib import admin
from django.contrib.auth.views import LoginView, LogoutView, PasswordChangeView, PasswordChangeDoneView, \
    PasswordResetView, PasswordResetDoneView, PasswordResetConfirmView, PasswordResetCompleteView
from django.urls import path, include
from django.views.generic import TemplateView

urlpatterns = [
    path('', include('metcalf.imas.frontend.urls')),
    path('accounts/login/', LoginView.as_view(template_name='imas/account/login.html'), name='login'),
    path('accounts/logout/', LogoutView.as_view(), name='logout'),
    path('accounts/password_change/', PasswordChangeView.as_view(), name='password_change'),
    path('accounts/password_change/done/', PasswordChangeDoneView.as_view(), name='password_change_done'),
    path('accounts/password_reset/', PasswordResetView.as_view(template_name='imas/account/password_reset.html'),
         name='password_reset'),
    path('accounts/password_reset/done/', PasswordResetDoneView.as_view(), name='password_reset_done'),
    path('accounts/reset/<uidb64>/<token>/', PasswordResetConfirmView.as_view(), name='password_reset_confirm'),
    path('accounts/reset/done/', PasswordResetCompleteView.as_view(), name='password_reset_complete'),

    path('accounts/profile/', TemplateView.as_view(template_name='imas/account/profile.html'), name="account_profile"),
    # path('accounts/login/', TemplateView.as_view(template_name='imas/account/login.html'), name="account_login"),
    path('admin/', admin.site.urls),
]
