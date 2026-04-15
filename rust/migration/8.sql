-- video_detail.created_date
UPDATE video_detail
SET created_date = strftime('%Y-%m-%dT%H:%M:%f', datetime(created_date / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(created_date) = 'integer';

-- video_detail.handle_time
UPDATE video_detail
SET handle_time = strftime('%Y-%m-%dT%H:%M:%f', datetime(handle_time / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(handle_time) = 'integer';

-- task.last_run_time
UPDATE task
SET last_run_time = strftime('%Y-%m-%dT%H:%M:%f', datetime(last_run_time / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(last_run_time) = 'integer';

-- dict.created_date
UPDATE dict
SET created_date = strftime('%Y-%m-%dT%H:%M:%f', datetime(created_date / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(created_date) = 'integer';

-- dict.last_modified_date
UPDATE dict
SET last_modified_date = strftime('%Y-%m-%dT%H:%M:%f', datetime(last_modified_date / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(last_modified_date) = 'integer';

-- config.created_date
UPDATE config
SET created_date = strftime('%Y-%m-%dT%H:%M:%f', datetime(created_date / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(created_date) = 'integer';

-- config.last_modified_date
UPDATE config
SET last_modified_date = strftime('%Y-%m-%dT%H:%M:%f', datetime(last_modified_date / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(last_modified_date) = 'integer';

-- associate_rule.created_date
UPDATE associate_rule
SET created_date = strftime('%Y-%m-%dT%H:%M:%f', datetime(created_date / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(created_date) = 'integer';

-- associate_rule.last_modified_date
UPDATE associate_rule
SET last_modified_date = strftime('%Y-%m-%dT%H:%M:%f', datetime(last_modified_date / 1000, 'unixepoch'), 'localtime')
    || '+08:00'
WHERE typeof(last_modified_date) = 'integer';