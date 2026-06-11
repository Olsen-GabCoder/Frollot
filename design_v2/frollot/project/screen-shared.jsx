// screens-shared.jsx — atomes UI partagés pour les maquettes Frollot
// Tous les styles s'appuient sur les tokens CSS (var(--…)).

// Table de codepoints Material Symbols (rendu par glyphe direct, sans ligature —
// fiable en navigateur ET dans les captures/exports).
const MS = {
  arrow_back:'e5c4', arrow_forward:'e5c8', chevron_left:'e5cb', chevron_right:'e5cc',
  menu:'e5d2', close:'e5cd', check:'e5ca', content_cut:'e14e', favorite:'e87e',
  place:'e55f', star:'e838', schedule:'e8b5', person:'e7fd', person_add:'e7fe',
  search:'e8b6', home:'e9b2', dynamic_feed:'ea14', calendar_month:'ebcc',
  notifications:'e7f5', more_horiz:'e5d3', add_circle:'e3ba', verified:'ef76',
  share:'e80d', swap_horiz:'e8d4', chat_bubble:'e0cb', bookmark:'e8e7',
  login:'ea77', mail:'e159', lock:'e899', visibility:'e8f4', visibility_off:'e8f5',
  palette:'e40a', spa:'eb4c', face:'f008', auto_awesome:'e65f', groups:'f233',
  check_circle:'f0be',
};

// Icône Material Symbols (par codepoint)
function Icon({ name, size = 24, color = 'var(--on-surface)', fill = 0, weight = 400, style = {} }) {
  const cp = MS[name];
  return (
    <span
      className="material-symbols-outlined"
      data-icon={name}
      style={{
        fontSize: size,
        color,
        fontVariationSettings: `'FILL' ${fill}, 'wght' ${weight}, 'GRAD' 0, 'opsz' 24`,
        lineHeight: 1,
        userSelect: 'none',
        ...style,
      }}
    >{cp ? String.fromCodePoint(parseInt(cp, 16)) : name}</span>
  );
}

// Placeholder image — bandes diagonales + légende mono
function Ph({ label = 'image', h = 160, r = 'var(--radius-md)', tone = 'primary', style = {} }) {
  const tints = {
    primary:   ['#EEE2F1', '#E2D2E8', '#6B4E78'],
    secondary: ['#F7E2EA', '#EFD0DC', '#A4677F'],
    tertiary:  ['#F4E9D4', '#EAD9BE', '#A98750'],
    neutral:   ['#EFEAEE', '#E4DCE3', '#897C8A'],
  }[tone];
  return (
    <div style={{
      height: h, borderRadius: r, overflow: 'hidden', position: 'relative',
      background: `repeating-linear-gradient(135deg, ${tints[0]} 0 11px, ${tints[1]} 11px 22px)`,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      ...style,
    }}>
      <span style={{
        fontFamily: 'ui-monospace, "SF Mono", Menlo, monospace',
        fontSize: 10.5, letterSpacing: .5, color: tints[2],
        background: 'rgba(255,255,255,.72)', padding: '3px 8px', borderRadius: 999,
        fontWeight: 600,
      }}>{label}</span>
    </div>
  );
}

// Avatar — cercle, initiales ou placeholder, bordure gradient optionnelle
function Avatar({ initials = 'F', size = 40, ring = false, tone = 'primary' }) {
  const bg = { primary: 'var(--primary)', secondary: 'var(--secondary)', tertiary: 'var(--tertiary)' }[tone];
  const inner = (
    <div style={{
      width: size, height: size, borderRadius: '50%', background: bg, color: '#fff',
      display: 'grid', placeItems: 'center', fontFamily: 'var(--font-sans)',
      fontWeight: 700, fontSize: size * 0.4, flexShrink: 0,
    }}>{initials}</div>
  );
  if (!ring) return inner;
  return (
    <div style={{
      padding: 2, borderRadius: '50%', flexShrink: 0,
      background: 'linear-gradient(135deg, var(--secondary), var(--primary))',
    }}>
      <div style={{ padding: 2, borderRadius: '50%', background: 'var(--surface)' }}>{inner}</div>
    </div>
  );
}

// Bouton
function Btn({ children, kind = 'primary', icon, full = false, style = {}, onClick }) {
  const kinds = {
    primary:   { background: 'var(--primary)', color: 'var(--on-primary)', border: 'none' },
    secondary: { background: 'var(--secondary-container)', color: 'var(--on-secondary-container)', border: 'none' },
    outline:   { background: 'transparent', color: 'var(--primary)', border: '1px solid var(--outline)' },
    text:      { background: 'transparent', color: 'var(--primary)', border: 'none' },
    tonal:     { background: 'var(--primary-container)', color: 'var(--on-primary-container)', border: 'none' },
  }[kind];
  return (
    <button onClick={onClick} style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8,
      padding: '0 24px', height: 48, minHeight: 48,
      borderRadius: 'var(--radius-full)', fontFamily: 'var(--font-sans)',
      fontWeight: 700, fontSize: 15, cursor: 'pointer',
      width: full ? '100%' : 'auto', ...kinds, ...style,
    }}>
      {icon && <Icon name={icon} size={20} color="currentColor" />}
      {children}
    </button>
  );
}

// Chip de filtre
function Chip({ children, selected = false, icon, onClick, style = {} }) {
  return (
    <button onClick={onClick} style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      padding: '8px 14px', height: 36, borderRadius: 'var(--radius-full)',
      fontFamily: 'var(--font-sans)', fontWeight: 600, fontSize: 13, cursor: 'pointer',
      whiteSpace: 'nowrap',
      background: selected ? 'var(--primary-container)' : 'var(--surface)',
      color: selected ? 'var(--on-primary-container)' : 'var(--on-surface-variant)',
      border: selected ? '1px solid transparent' : '1px solid var(--outline-variant)',
      ...style,
    }}>
      {icon && <Icon name={icon} size={16} color="currentColor" />}
      {children}
    </button>
  );
}

// Badge de statut
function StatusBadge({ children, tone = 'success', icon }) {
  const tones = {
    success: ['var(--success-container)', 'var(--on-success-container)'],
    warning: ['var(--warning-container)', 'var(--on-warning-container)'],
    error:   ['var(--error-container)', 'var(--on-error-container)'],
    info:    ['var(--info-container)', 'var(--on-info-container)'],
    primary: ['var(--primary-container)', 'var(--on-primary-container)'],
  }[tone];
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 4,
      padding: '4px 10px', borderRadius: 'var(--radius-full)',
      background: tones[0], color: tones[1],
      fontFamily: 'var(--font-sans)', fontWeight: 700, fontSize: 11.5,
    }}>
      {icon && <Icon name={icon} size={14} color="currentColor" fill={1} />}
      {children}
    </span>
  );
}

// Notation étoiles
function Stars({ value = 5, size = 16 }) {
  return (
    <span style={{ display: 'inline-flex', gap: 1 }}>
      {[1, 2, 3, 4, 5].map(i => (
        <Icon key={i} name="star" size={size} fill={i <= Math.round(value) ? 1 : 0}
          color={i <= Math.round(value) ? 'var(--tertiary)' : 'var(--outline-variant)'} />
      ))}
    </span>
  );
}

// En-tête de marque standard (remplace l'app bar du device)
function BrandHeader({ title, leading = 'menu', trailing, sub, onSurface = true }) {
  return (
    <div style={{
      padding: '8px 8px 8px 12px', display: 'flex', alignItems: 'center', gap: 4,
      background: 'var(--surface)', minHeight: 56,
    }}>
      <button style={iconBtn}><Icon name={leading} size={24} /></button>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div className="t-title-lg" style={{ color: 'var(--on-surface)', lineHeight: 1.1 }}>{title}</div>
        {sub && <div className="t-body-sm" style={{ color: 'var(--on-surface-variant)' }}>{sub}</div>}
      </div>
      {trailing}
    </div>
  );
}

const iconBtn = {
  width: 44, height: 44, minWidth: 44, borderRadius: '50%', border: 'none',
  background: 'transparent', cursor: 'pointer', display: 'grid', placeItems: 'center',
  flexShrink: 0,
};

// Barre de navigation inférieure (5 onglets)
function BottomNav({ active = 0 }) {
  const tabs = [
    { icon: 'home', label: 'Accueil' },
    { icon: 'dynamic_feed', label: 'Social' },
    { icon: 'search', label: 'Explorer' },
    { icon: 'calendar_month', label: 'RDV' },
    { icon: 'person', label: 'Profil' },
  ];
  return (
    <div style={{
      display: 'flex', background: 'var(--surface-container)', padding: '10px 4px 12px',
      borderTop: '1px solid var(--outline-variant)',
    }}>
      {tabs.map((t, i) => {
        const on = i === active;
        return (
          <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 }}>
            <div style={{
              padding: '4px 18px', borderRadius: 'var(--radius-full)',
              background: on ? 'var(--secondary-container)' : 'transparent',
              display: 'grid', placeItems: 'center',
            }}>
              <Icon name={t.icon} size={22} fill={on ? 1 : 0}
                color={on ? 'var(--on-secondary-container)' : 'var(--on-surface-variant)'} />
            </div>
            <span style={{
              fontFamily: 'var(--font-sans)', fontSize: 11, fontWeight: on ? 700 : 600,
              color: on ? 'var(--on-surface)' : 'var(--on-surface-variant)',
            }}>{t.label}</span>
          </div>
        );
      })}
    </div>
  );
}

Object.assign(window, { Icon, MS, Ph, Avatar, Btn, Chip, StatusBadge, Stars, BrandHeader, BottomNav, iconBtn });
