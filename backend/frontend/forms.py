from django import forms

from backend.models import DocumentAttachment


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
