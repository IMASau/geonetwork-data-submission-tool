import json
from data_migration import migrate_data

input_filepath = input('Please input the filepath to the data you wish to migrate (leave blank for default): ')
if len(input_filepath) == 0:
    input_filepath = 'data_migration_tool/aodn/input.json'
input_data = json.loads(open(input_filepath, 'r').read())

template_filepath = input('Please input the filepath to the output template data (leave blank for default): ')
if len(template_filepath) == 0:
    template_filepath = 'data_migration_tool/aodn/template.json'
template = json.loads(open(template_filepath, 'r').read())

migrations_filepath = input('Please input the filepath to the list of migrations you wish to make (leave blank for default): ')
if len(migrations_filepath) == 0:
    migrations_filepath = 'data_migration_tool/aodn/migrations.json'
migrations = json.loads(open(migrations_filepath, 'r').read())

output_filepath = input('Please input the filepath to where you wish to output the migrated data (leave blank for default): ')
if len(output_filepath) == 0:
    output_filepath = 'data_migration_tool/aodn/output.json'

output_data = migrate_data(input_data, template, migrations)

open(output_filepath, 'wt').write(json.dumps(output_data))