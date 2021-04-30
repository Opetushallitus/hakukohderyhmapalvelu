const webpack = require('@cypress/webpack-preprocessor')
const { Client } = require('pg')

const configDb = env => {
  return {
    user: env.POSTGRES_USER,
    host: 'localhost',
    database: env.POSTGRES_DATABASE,
    password: env.POSTGRES_PASSWORD,
    port: env.POSTGRES_PORT,
  }
}

const createDbClient = config => {
  return new Client(configDb(config.env))
}

module.exports = (on, config) => {
  const options = {
    webpackOptions: {
      resolve: {
        extensions: ['.ts', '.tsx', '.js'],
      },
      module: {
        rules: [
          {
            test: /\.tsx?$/,
            loader: 'ts-loader',
            options: { transpileOnly: true },
          },
        ],
      },
    },
  }
  const dbClient = createDbClient(config)
  dbClient.connect()
  on('file:preprocessor', webpack(options))
  on('task', {
    query({ sql, values }) {
      return dbClient.query(sql, values)
    },
  })
}
