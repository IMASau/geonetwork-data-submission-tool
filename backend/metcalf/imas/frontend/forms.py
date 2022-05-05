from django import forms
from captcha.fields import ReCaptchaField
from captcha.widgets import ReCaptchaV2Checkbox

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
            widget=ReCaptchaV2Checkbox(
                attrs={
                        'data-theme': 'light',  # default=light
                        'data-size': 'normal',  # default=normal
                },
            ),
    )
    field_order = ['email', 'username', 'password1', 'password2', 'captcha']
 
    def signup(self, request, user):
        pass