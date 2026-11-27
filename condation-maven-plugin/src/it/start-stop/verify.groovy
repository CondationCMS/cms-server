def home = new File(basedir, 'cms-home')

assert new File(home, 'modules/it-module/module.properties').isFile() : 'module was not installed'
assert !new File(home, 'cms.pid').exists() : 'server still registered after condation:stop'

def log = new File(home, 'logs/plugin-server.out')
assert log.isFile() : 'no server log'
assert log.text.contains('cms startup successfully') : 'server did not report a successful startup'
return true
