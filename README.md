# CondationCMS

CondationCMS is a fast, flexible, and developer-friendly content management system built with Java.

Content is stored in Markdown files with YAML front matter instead of a database. The file and directory structure remains transparent, easy to version with Git, and directly represents the structure of the website.

CondationCMS combines a powerful template system, a modular Java architecture, JavaScript extensions, multisite support, and an integrated management application for editors.

## Features

* **File-based content** — Manage content as Markdown files with YAML front matter
* **Developer-friendly architecture** — Extend the system using Java modules or JavaScript extensions
* **Powerful template system** — Build dynamic websites using templates, components, filters, functions, and custom tags
* **Manager application** — Create and edit content through a browser-based interface
* **Multisite support** — Run multiple independent websites on one server
* **Multilingual websites** — Build and manage sites in multiple languages
* **Hooks and events** — Customize processing and react to CMS lifecycle events
* **Integrated search and indexing** — Query and organize content efficiently
* **Media processing** — Manage and transform images and other media assets
* **Git-friendly workflow** — Version, review, and deploy content using standard Git workflows
* **Integrated caching** — Optimized content and template processing for fast page delivery

## Getting started

Getting CondationCMS running locally only requires a few steps.

### 1. Configure the Manager

Create an `.env` file in the root directory of the server installation.

Add a strong, randomly generated secret:

```env
CMS_UI_SECRET=<your-random-secret>
```

For example:

```env
CMS_UI_SECRET=xnK82mcK7I9s_K3j-L8vK9L2m_N3o_P4q_R5s_T6u_V7w_X8y_Z9a_B0c_D1e
```

The secret is used to secure the Manager application and should be unique for every installation.

### 2. Create a Manager user

Create a user with access to the Manager application:

```bash
./server.sh server add_user \
  -r=manager-users \
  -ro=manager \
  <username> \
  <password> \
  <email>
```

Replace `<username>`, `<password>`, and `<email>` with the account details you want to use.

### 3. Install the modules

The demo setup depends on additional modules. Install all available modules with:

```bash
./server.sh module get-all
```

You can skip this step when using a custom setup that does not require the demo modules.

### 4. Start the server

Start CondationCMS:

```bash
./server.sh server start
```

Once the server has started, open the website in your browser:

```text
http://localhost:2020
```

The Manager login is available at:

```text
http://localhost:2020/manager/login
```

Sign in with the Manager user created in the previous step.

## Documentation

Detailed information about installing, configuring, using, and extending CondationCMS is available in the official [CondationCMS documentation](https://condation.com/documentation).

## License

CondationCMS is licensed under the [GNU Affero General Public License v3.0](./LICENSE).

Modules, plugins, themes, and extensions that use only the public and documented extension APIs may be distributed under a different license, including proprietary or commercial licenses.

See [LICENSE-EXCEPTION.md](./LICENSE-EXCEPTION.md) for details.