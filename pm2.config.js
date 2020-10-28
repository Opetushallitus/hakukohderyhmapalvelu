const config =
  process.env.CONFIG ||
  '../local-environment/oph-configurations/pallero/oph-configuration/hakukohderyhmapalvelu.config.edn'

module.exports = {
  apps: [
    {
      name: 'hakukohderyhmapalvelu-frontend',
      script: 'lein',
      interpreter: '/bin/sh',
      args: ['frontend:dev'],
      cwd: __dirname,
      log_file: 'logs/hakukohderyhmapalvelu-frontend.log',
      pid_file: 'pids/hakukohderyhmapalvelu-frontend.pid',
      combine_logs: true,
      min_uptime: 30000,
      max_restarts: 5,
      restart_delay: 4000,
      wait_ready: true,
      watch: false,
      exec_interpreter: 'none',
      exec_mode: 'fork',
    },
    {
      name: 'hakukohderyhmapalvelu-backend',
      script: 'lein',
      interpreter: '/bin/sh',
      args: ['server:dev'],
      env: {
        TIMBRE_NS_BLACKLIST: '["clj-timbre-auditlog.audit-log"]',
        CONFIG: config,
      },
      cwd: __dirname,
      log_file: 'logs/pm2/hakukohderyhmapalvelu-backend.log',
      pid_file: 'pids/hakukohderyhmapalvelu-backend.pid',
      combine_logs: true,
      min_uptime: 30000,
      max_restarts: 5,
      restart_delay: 4000,
      wait_ready: true,
      watch: false,
      exec_interpreter: 'none',
      exec_mode: 'fork',
    },
  ],
}
