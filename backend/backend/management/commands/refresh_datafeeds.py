from django.core.management.base import BaseCommand
from django.core.management import call_command

from backend.models import DataFeed
from cStringIO import StringIO

class Command(BaseCommand):
    help = "Run any scheduled datafeed refresh requests.  Run via a cron job."

    def handle(self, *commands, **options):

        for datafeed in DataFeed.objects.filter(state=DataFeed.SCHEDULED)[:1]:
            datafeed.start()
            datafeed.save()
            buf = StringIO()
            try:
                call_command(datafeed.name, stdout=buf)
                buf.seek(0)
                datafeed.success("Success.\n\n"+buf.read())
                datafeed.save()
            except:
                datafeed.failure("Failure.\n\n"+buf.read())
                datafeed.save()
                raise
