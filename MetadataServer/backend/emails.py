from django.template.loader import render_to_string
from django.core.mail import send_mail

from django.contrib.sites.models import Site
from django.conf import settings
from django.template import Context, Template


def get_site(doc):
    return doc.template.site or Site.objects.get(id=settings.SITE_ID)


def email_manager_submit_alert(doc):
    """
    1. New metadata submitted (email to data manger)
    """
    site = get_site(doc)
    context = {
        'document': doc,
        'site': site
    }
    send_mail(subject="New metadata record submitted: {0}".format(doc.uuid),
              message=render_to_string('email_manager_submit_alert.txt', context),
              from_email=doc.owner.email,
              recipient_list=[site.sitecontent.email],
              fail_silently=False,
              html_message=render_to_string('email_manager_submit_alert.html', context))


def email_user_submit_confirmation(doc):
    """
    2. New metadata submitted (email to user)
    """
    site = get_site(doc)
    context = {
        'document': doc,
        'site': site
    }
    send_mail(subject="Metadata submission confirmed: {0}".format(doc.title),
              message=render_to_string('email_user_submit_confirmation.txt', context),
              from_email=site.sitecontent.email,
              recipient_list=[doc.owner.email],
              fail_silently=False,
              html_message=render_to_string('email_user_submit_confirmation.html', context))


def email_manager_updated_alert(doc):
    """
    3. Submitted metadata has been modified (email to Data Manger)

    """
    site = get_site(doc)
    context = {
        'document': doc,
        'site': site
    }
    send_mail(subject="Metadata edited: {0}".format(doc.uuid),
              message=render_to_string('email_manager_updated_alert.txt', context),
              from_email=doc.owner.email,
              recipient_list=[site.sitecontent.email],
              fail_silently=False,
              html_message=render_to_string('email_manager_updated_alert.html', context))


def email_user_upload_alert(doc):
    """
    4. Record status changed to "uploaded" by Data Manger
    """
    site = get_site(doc)
    context = {
        'document': doc,
        'site': site
    }
    context['portal_record_url'] = Template(site.sitecontent.portal_record_url).render(Context(context)).strip()
    send_mail(subject="Your data is now available for discovery in the {0}".format(site.sitecontent.portal_title),
              message=render_to_string('email_user_upload_alert.txt', context),
              from_email=site.sitecontent.email,
              recipient_list=[doc.owner.email],
              fail_silently=False,
              html_message=render_to_string('email_user_upload_alert.html', context))
