import { h as s } from "preact";
import { UIPlugin as r } from "@uppy/core";
import { Provider as t } from "@uppy/companion-client";
import { ProviderViews as e } from "@uppy/provider-views";
const n = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACQAAAAiCAYAAAA3WXuFAAABJGlDQ1BJQ0MgUHJvZmlsZQAAeJydkb1KxEAUhU9W8Q/FQrEQixS2CzamslkVgrBCjCsYrbIzWVzMjCGZZfENfBN9mC0EwTfwBRSsPTdaWDiNM1zux+Xec+YH6ISlMs38HmCsq+O0l11mV+HiG5a5gQ2s56qpeknSh3d9viKQ/NIVLX/fn2tBF41injGsqmoHBAfkaOoqYQY2bwfpEfmBHGpjNfmJvKuNFpbZ1JQT9aMpp1kt7MW51Bk7iHGCUyQIMcQEY5Rw6DJbVo4RYZ85Ro0c92igmEsUrE3Z43BDaqgU45A0IPE0Hr/t1i+hy5AaY2qJwx0MNcUP8r7fYx9n7WSwNavyOm9Lc4zOaAS8PwJrGT/gGVi59ngt/b6bpydqe/55xy+Q1FA5iW7RvgAACUNJREFUeJy9WG1QVNcZfs49ywpGEhILOgOKzMCoDLVxSJuBERmZGBtCOhi9MWPLxGknTJo2TWxMJ8Zktp1AEsZk1R8JEYqmY3QKGsGaSikqFDPGImBYici6yvdHICywK1v27t59+oO7ZDUaP6bpmbkz9559z3ue834873sWuINBUhqPAgBOpzOepJ2ki2Q9yTdIvqRpWqohrwCA3W6fRbLL5XKNLl26NAmAsFgsyp3sfT0QEVQeMreMZJff72d9fT33798/2N7e7iVJTdNcjzzyyAqSwpBNI+knycLCwhdJirq6OtNdgwl5X0NyVV1dXTjJ8x9//HEgPT19O4CnAIQBiCXZWVVVxcjIyJywsLDgum2tra10OByjjY2Np425O7eQ4R7hdDrvI/lnfjO6STI9Pf1DAAgLC4PJZALJ5SS5YsWKUmN9uGHdA++///5AZWXlFpJsaWlZfcegQiwjSNbb7XZmZGQc3r59++ampqZLhw4d8iclJeVXVFRIVVUjLBaLQjJuampqwO12N+zduzc8RFeD1Wq1q6oa4fF4/uZyuSZWr16dQPL2Ysk4lSC5UNO0cpLMy8v7LEREAlgKYFZwIqi4rq4u0eVydfj9/naSiw0rjz733HM1hu55brebGzZs+K2x7taxRFIhafb5fE0keeLECSYmJv7BZDLhhRdemCWlhKIoEEJ8ax0AqKo6p7+//+jVq1cDJO0Oh4OxsbGbhRAgGeHxeEZ27dq1i6TyncFtWCUMAAYGBqJJXi0uLp6IiYl5G4BiWEEYT/D9hqAAYN++fc/s2bOnKS0t7RCA+0hKQ+ZIVVWVzXiXNwMy80N+fn4YyfcuX75MAC8BQEVFxQwHBWnAON2NQInrNzKsYzJ+f9nr9fKrr75KD+oOXSxD3ueSzCd5sauriytXrqyPj48Pv9nGtzOM7FMwnRyCpDI+Pn5/b2/vcb/fz0uXLqmKolybcaOjowtIlk1NTU11dnZy27Ztw1FRUVsB3Esy6CpomvYiyeMkbSTPkHy7pqbmHiMjgyQojY2X+/3+l1VVNRtglOvT3Ov1ftjT06Nh2p0KAAUknyLZX1tby3Xr1v0dQB6AH9zotCS7HQ4H33rrrc6Wlpb+QCDAlJSUl4EZN88wOsktJBkbG6tep0a0tbWZAcBsNoPkVENDw2sAUFdXZwJJFhUV/QfAWpKbSNaS7CM5out6w/nz53NCTt907tw5n9VqzSotLZ23YMGCTQBSQ3dra2ubQzKC5GZN03xxcXG/AgCn0/lDkv8k6SA5QrJjcHCwoKen5x2SLCsryyQp0NHREZg9e3a+EXTHOjo6uGXLli+sVuspkvzggw+6AEhFUeB2u7OGhoY8BltrJIOpLEg+SPI0yUFd1z0kJ4eHh7lkyZJnDffkkaTVanVZLJYmu90+2N3dTQAPk6z+8ssvOwDAFBkZKbKysno//fRTeezYsZKSkhLfokWLXly1apUPQJumaQEAAV3XxdjYWHN2dnaGlHJ5cXFxbmpqqvXRRx/tF0JU+P3+B6WUaQUFBSNut/tMXl5eXEJCQpLP54PhHg8A3Wq1vtfX1/cnAFFnz57dGhER0QdgU3JycldLS8s7IKnX19fvAABd1/eSHGbI2LFjRwcAIaUEyR6SxwxrbCOpPf74428b7txA0p+UlPS68f0bv9/P+Pj4Xxrf60lyzZo1fwxynTEfDIfnjx49Oh3ZmqY9o6qqWVGUWTU1NdHp6ek7169f/w6AKSGEDMZQf39/49jY2GN+v//c5ORkwfHjx8McDsdkMFgByJSUlDCSswDMDgQC1+SEruu8cOHClBDCV1VVFel2u5dJKWmA+mJ8fJwmAOejoqKWHDx40AfAnJCQwNOnT/8LgAlAIDw8XALTxBYXF7dx/vz5WTExMYucTqfs6+vrjYuLC9Y4LRAIBFpbWyeFEF6SbpIEQMP64VJKUV5e/nBaWtovADw5NDSUEwgEHhJC2Ej6NE2DCcCTqampF6urq9WJiYn6QCCwbmxsrFLXdbS3t6O6utoJgD6fT0gptaGhoX8MDQ0BABRFQW9vrzTAz1YURTly5MjKlJSUSQDZZrNZCCHCAGBiYqK1u7t7MCEhIXd0dDR3fHwcn3zyCaSUD+i6HrTwjB8LBgYGJgBEAkiVUj4BYC2ALADzgZnScVO29ng88WfOnOm6cuUKh4eH2drayqKiIn3OnDnrQopwDIAsKeVaAE8AWL558+YII0ufPnjwYCCYstGaprG4uPjZoHsAQEoZpP6ZikwylmQeyQMkz5H8N8mtJGMBmAE8DODHADIALA6e3GKxKIpybesTWjJI/qW8vJwztYzkAZvN5khMTJzV1tZmNig/1IoPkTzlcrlot9u5Z88evbCw0HbgwIHPL1y44PV6vST5M0P2MZImIzNDrSpUVZWZmZkmADI/Pz/MkJ9P0rVx48aLIGkyrJRjVPdlISDmkfyRYZGJkpISZmdnfwRAxbXlJaq7u/tNTdMCJE8aBLjPYrGYeKP24hv9EoBCsry2tpYA1oa2BK9//fXXPgDLSN5DssLtduvDw8Ps7+/nK6+8Mg7gp0FlhisFSUXK6T29Xm/V5OQkfT4fKysrPzf03jDuQjyzzuPxcPHixR8JITADyKjk1DTNQbLPZrMxNze3OjU19dcRERG5ABYqihJsOWe4CZiOD053BfcmJyf/3GazFZD0Nzc3P8kb9EbBuZ6engiSNovF4sR0ZzGjUwDAzp07ny8tLa0tKiqyzZ07tzBUiWGF274lkGytrq4eDAIIafBksG0l+ebly5c5b968rVJKqKp6U/fOnCQzM9NkCN4SDElh3DYEyd95vV62tbX95CaySSRHN23adBaAyci2bzXpwVOIW6L9DlAklaamptlut7vK7/dT1/WLJD8j+S7Jp0l+rus6r1y5wqSkpNeEEDAy7+7a0tsBJYQgAFFUVPR7s9mcFR0d/UBGRsbihQsX3r97926cOnWqvKWlxdbe3l5GctjgPn4feEJBzXwXFhZGNzc3P6Gq6jYAad/bxrcYwvjnQ5L8q9HRdJA8UVZWlmyxWEx3Gxp3PUJ47o3GxsbAq6++eowkVVV9F7jNm+v/GFCQAFeMjIwESO632WyMiop6JjSY/5+ABACls7Mz/OTJkw2HDx9mTk5OB4BIhlyfguO/9ObRuRUYEGEAAAAASUVORK5CYII=";
class d extends r {
  constructor(A, i) {
    super(A, i), this.id = this.opts.id || "ASDC", t.initPlugin(this, i), this.title = this.opts.title || "ASDC", this.type = "acquirer", this.icon = () => /* @__PURE__ */ s("img", { alt: "asdc logo", width: "32", height: "32", src: n }), this.provider = new t(A, {
      companionUrl: this.opts.companionUrl,
      companionHeaders: this.opts.companionHeaders,
      provider: "asdc",
      pluginId: this.id
    }), this.defaultLocale = {
      strings: {
        pluginNameASDC: "ASDC"
      }
    }, this.i18nInit(), this.title = this.i18n("pluginNameASDC"), this.files = [];
  }
  install() {
    this.view = new e(this, {
      provider: this.provider
    });
    const { target: A } = this.opts;
    A && this.mount(A, this);
  }
  uninstall() {
    this.view.tearDown(), this.unmount();
  }
  onFirstRender() {
    return this.view.getFolder();
  }
  render(A) {
    return this.view.render(A);
  }
}
export {
  d as ASDC
};
