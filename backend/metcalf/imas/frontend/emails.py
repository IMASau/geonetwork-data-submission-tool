from django.conf import settings
from django.contrib.sites.models import Site
from django.core.mail import EmailMultiAlternatives
from django.template import Context, Template
from django.template.loader import render_to_string

def get_site(doc):
    return doc.template.site or Site.objects.get(id=settings.SITE_ID)


def send_mail(subject=None, message=None, html_message=None, from_email=None, recipient_list=None, fail_silently=False):
    """
    Re-implements the standard send_mail implementation, so that we can
    send on behalf of the notional sender but from a fixed
    application-email address (also useful when the configured SMTP
    relay performs address verification)

    """
    metcalf_from = settings.METCALF_FROM_EMAIL or settings.DEFAULT_FROM_EMAIL
    msg = EmailMultiAlternatives(subject=subject,
                                 body=message,
                                 from_email=metcalf_from,
                                 reply_to=[from_email],
                                 to=recipient_list)
    if html_message:
        msg.attach_alternative(html_message, 'text/html')
    msg.send(fail_silently=fail_silently)
    breakpoint()


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