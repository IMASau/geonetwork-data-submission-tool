from django.core.management.base import BaseCommand, CommandError

from backend.models import DataFeed

class Command(BaseCommand):
    help = "Run any scheduled datafeed refresh requests.  Run via a cron job."

    def add_arguments(self, parser):
        parser.add_argument('args', metavar='datafeeds', nargs='*', help='Datafeed names')

        parser.add_argument(
            '-a', '--all', dest='all', default=False, action='store_true',
            help='Schedule all idle data feeds',
        )

    def handle(self, *names, **options):

        datafeeds = DataFeed.objects.filter(state=DataFeed.IDLE)

        if names and options['all']:
            raise CommandError("Names are ignored if all flag is used.")

        if not options['all']:
            datafeeds = datafeeds.filter(name__in=names)

        for datafeed in datafeeds:
            datafeed.schedule()
            datafeed.save()
