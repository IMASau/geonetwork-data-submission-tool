from captcha.fields import ReCaptchaField
from captcha.widgets import ReCaptchaV3
from django import forms
from django.conf import settings

from metcalf.imas.backend.models import DocumentAttachment


class SiteContentForm(forms.ModelForm):
    def __init__(self, *args, **kwargs):
        super(SiteContentForm, self).__init__(*args, **kwargs)
        self.fields['portal_record_url'].widget = forms.Textarea(attrs={'cols': '60', 'rows': '1'})


class DocumentAttachmentForm(forms.ModelForm):

    def clean_name(self):
        return self.cleaned_data['name'].replace(" ", "_")

    class Meta:
        model = DocumentAttachment
        fields = ('document', 'name', 'file')


class MySignupForm(forms.Form):

    captcha = ReCaptchaField(
        label='',
        widget=ReCaptchaV3(),
    )
    # Work-around in testing mode; see
    # https://github.com/torchbox/django-recaptcha/issues/157#issuecomment-406301677=
    if getattr(settings, "DEBUG", False):
        captcha.clean = lambda x: True

    field_order = ['email', 'username', 'password1', 'password2',]

    def signup(self, request, user):
        pass
