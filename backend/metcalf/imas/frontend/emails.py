from django.conf import settings
from django.contrib.sites.models import Site
from django.core.mail import EmailMultiAlternatives
from django.template import Context, Template
from django.template.loader import render_to_string
from metcalf.common.emails import get_site, send_mail

def email_user_submit_confirmation(doc):
    """
    New metadata submitted (email to user)
    """
    site = get_site(doc)
    context = {
        'document': doc,
        'site': site
    }

    send_mail(subject=u"Metadata submission confirmed: {0}".format(doc.title),
              message=render_to_string('account/email/email_user_submit_confirmation.txt', context),
              from_email=site.sitecontent.email,
              recipient_list=[doc.owner.email],
              fail_silently=False,
              html_message=render_to_string('account/email/email_user_submit_confirmation.html', context))
