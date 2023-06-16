/** @jsx h */
import { h } from 'preact'
import { UIPlugin } from "@uppy/core";
import { Provider } from "@uppy/companion-client";
import { ProviderViews } from "@uppy/provider-views";


import logo from "./asdc.png";

export class ASDC extends UIPlugin {
  constructor(uppy, opts) {
    super(uppy, opts);
    this.id = this.opts.id || "ASDC";
    Provider.initPlugin(this, opts);
    this.title = this.opts.title || "ASDC";
    this.type = "acquirer";

    this.icon = () => <img alt="asdc logo" width="32" height="32" src={logo} />;
    // this.icon = () => {
    //   return (
    //     <svg width="32" height="32" xmlns="http://www.w3.org/2000/svg">
    //       <path d="M10 9V0h12v9H10zm12 5h10v18H0V14h10v9h12v-9z" fill="#000000" fillRule="nonzero" />
    //     </svg>
    //   );
    // };

    this.provider = new Provider(uppy, {
      companionUrl: this.opts.companionUrl,
      companionHeaders: this.opts.companionHeaders,
      provider: "asdc",
      pluginId: this.id,
    });

    this.defaultLocale = {
      strings: {
        pluginNameASDC: "ASDC",
      },
    };

    // merge default options with the ones set by user
    // this.opts = { ...defaultOptions, ...opts }

    this.i18nInit();
    this.title = this.i18n("pluginNameASDC");

    this.files = [];
  }

  install() {
    this.view = new ProviderViews(this, {
      provider: this.provider,
    });

    const { target } = this.opts;
    if (target) {
      this.mount(target, this);
    }
  }

  uninstall() {
    this.view.tearDown();
    this.unmount();
  }

  onFirstRender() {
    return this.view.getFolder();
  }

  render(state) {
    return this.view.render(state);
  }
}
