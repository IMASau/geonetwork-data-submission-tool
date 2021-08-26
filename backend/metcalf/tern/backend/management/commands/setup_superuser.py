import os

from django.contrib.auth.management.commands import createsuperuser
from django.core.management import CommandError
from django.db.utils import IntegrityError


class Command(createsuperuser.Command):
    help = 'Crate a superuser, and allow password to be provided'

    def add_arguments(self, parser):
        super(Command, self).add_arguments(parser)
        parser.add_argument(
            '--password', dest='password', default=None,
            help='Specifies the password for the superuser.',
        )

    def handle(self, *args, **options):
        password = os.environ.get('DJANGO_SUPERUSER_PASSWORD', options.get('password'))
        username = os.environ.get('DJANGO_SUPERUSER_USERNAME', options.get('username'))
        email = os.environ.get('DJANGO_SUPERUSER_EMAIL')
        database = options.get('database')

        if password and not username:
            raise CommandError("--username is required if specifying --password")

        # make sure we pass in 'username' in case it came from environment
        options['username'] = username
        options['email'] = email

        try:
            super(Command, self).handle(*args, **options)
        except IntegrityError:
            # all good user already exists, just update pw?
            if options['verbosity'] >= 1:
                self.stdout.write("Superuser already exists.")

        if password:
            user = self.UserModel._default_manager.db_manager(database).get(username=username)
            user.set_password(password)
            user.email = email
            user.save()