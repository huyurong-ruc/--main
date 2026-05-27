const fs = require('fs');
let schema = fs.readFileSync('db/kingbase_schema.sql', 'utf8');

// The lines we want are roughly from 'create table campus.wf_instance' to the end of 'cert_generated_file'
let startIndex = schema.indexOf('create table campus.wf_instance');
let endIndex = schema.indexOf('create table campus.aca_program');
let extracted = schema.substring(startIndex, endIndex);

extracted = extracted.replace(/campus\./g, '');
extracted = extracted.replace(/varchar2\((\d+) char\)/g, 'VARCHAR($1)');
extracted = extracted.replace(/number\(10\)/g, 'INTEGER');
extracted = extracted.replace(/number\(1\)/g, 'INTEGER');
extracted = extracted.replace(/number\((\d+),\s*(\d+)\)/g, 'DECIMAL($1, $2)');
extracted = extracted.replace(/clob/g, 'TEXT');
extracted = extracted.replace(/systimestamp/g, 'CURRENT_TIMESTAMP');
extracted = extracted.replace(/sys_user/g, 'user_account');
extracted = extracted.replace(/file_object/g, 'platform_file_upload_record');

// Note: `check (payload_json is json)` might fail in postgres if payload_json is TEXT. Postgres JSON check is different or the column should be JSON/JSONB.
// In earlier V20, `check (ext_json is json)` was removed by another script or it passed? Wait, we removed it in V20. So let's remove it here too.
extracted = extracted.replace(/check \([a-z_]+ is json\),?/g, '');
// Clean up any trailing commas from removing the json check
extracted = extracted.replace(/,\s*\)/g, '\n)');

fs.writeFileSync('src/main/resources/db/migration/V21__add_affair_and_workflow_tables.sql', extracted);
console.log('Done');
