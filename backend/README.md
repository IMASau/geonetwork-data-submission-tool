
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

Additional steps when setting up locally:

### Load external sources
```
python manage.py load_parameterunits
python manage.py load_parameterplatforms
python manage.py load_parameternames
python manage.py load_parameterinstruments
python manage.py load_institutions
python manage.py load_persons
python manage.py load_rolecodes
python manage.py load_samplingfrequencies
python manage.py load_topiccategories
python manage.py load_sciencekeywords
```

---

For production you'll want to add a `webapp/local_settings.py` and configure a 
different database, static file hosting details etc.

