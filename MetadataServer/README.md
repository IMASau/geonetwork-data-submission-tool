
This is a Django based web application.

Typical install steps would be:

```sh
cd MetadataServer
virtualenv venv
source venv/bin/activate
pip install -r requirements.txt
python manage.py syncdb
python manage.py runserver
```

For production you'll want to add a `webapp/local_settings.py` and configure a 
different database, static file hosting details etc.

