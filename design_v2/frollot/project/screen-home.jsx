// screen-home.jsx — Accueil Frollot
function HomeScreen() {
  const salons = [
    { n: 'Salon Lumière', v: 'Paris 9e · 0,8 km', r: 4.9, tone: 'primary' },
    { n: 'Studio Métamorphose', v: 'Paris 11e · 1,4 km', r: 4.8, tone: 'secondary' },
    { n: 'Atelier Sève', v: 'Paris 3e · 2,1 km', r: 4.7, tone: 'tertiary' },
  ];
  const cats = [
    { i: 'content_cut', l: 'Coupe' }, { i: 'palette', l: 'Coloration' },
    { i: 'spa', l: 'Soin' }, { i: 'face', l: 'Barbe' }, { i: 'auto_awesome', l: 'Coiffage' },
  ];
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: 'var(--background)' }}>
      {/* Header */}
      <div style={{ background: 'var(--surface)', padding: '6px 8px 14px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, minHeight: 52 }}>
          <button style={iconBtn}><Icon name="menu" /></button>
          <div style={{ flex: 1, fontFamily: 'var(--font-display)', fontSize: 24, fontWeight: 600, letterSpacing: .5 }}>Frollot</div>
          <button style={iconBtn}><Icon name="notifications" /></button>
          <Avatar initials="C" size={36} ring />
        </div>
        {/* Recherche */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 12, height: 52, margin: '6px 8px 0', padding: '0 18px',
          borderRadius: 'var(--radius-full)', background: 'var(--surface-container-high)',
        }}>
          <Icon name="search" size={22} color="var(--on-surface-variant)" />
          <span className="t-body-md" style={{ color: 'var(--on-surface-variant)' }}>Salon, coiffeur, prestation…</span>
        </div>
      </div>

      {/* Contenu scrollable */}
      <div style={{ flex: 1, overflow: 'auto', padding: '20px 0 16px' }}>
        {/* Salutation */}
        <div style={{ padding: '0 20px', marginBottom: 18 }}>
          <div className="t-overline" style={{ color: 'var(--secondary)' }}>Mercredi 2 juin</div>
          <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 30, fontWeight: 600, lineHeight: 1.05, color: 'var(--on-surface)', marginTop: 4 }}>
            Bonjour Camille,<br/>prête à vous sublimer&nbsp;?
          </h2>
        </div>

        {/* Catégories */}
        <div style={{ display: 'flex', gap: 10, overflow: 'auto', padding: '0 20px 4px', marginBottom: 24 }}>
          {cats.map((c, i) => (
            <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8, flexShrink: 0 }}>
              <div style={{
                width: 58, height: 58, borderRadius: 'var(--radius-lg)',
                background: i === 0 ? 'var(--primary)' : 'var(--surface)',
                border: i === 0 ? 'none' : '1px solid var(--outline-variant)',
                display: 'grid', placeItems: 'center', boxShadow: 'var(--elev-1)',
              }}>
                <Icon name={c.i} size={26} color={i === 0 ? 'var(--on-primary)' : 'var(--primary)'} />
              </div>
              <span className="t-label-md" style={{ color: 'var(--on-surface-variant)', letterSpacing: .2, textTransform: 'none', fontSize: 12 }}>{c.l}</span>
            </div>
          ))}
        </div>

        {/* Salons récents */}
        <SectionHead title="Salons récents" />
        <div style={{ display: 'flex', gap: 14, overflow: 'auto', padding: '14px 20px 4px' }}>
          {salons.map((s, i) => (
            <div key={i} style={{ width: 220, flexShrink: 0, background: 'var(--surface)', borderRadius: 'var(--radius-lg)', overflow: 'hidden', boxShadow: 'var(--elev-1)', border: '1px solid var(--outline-variant)' }}>
              <div style={{ position: 'relative' }}>
                <Ph label="vitrine salon" h={132} r={0} tone={s.tone} />
                <div style={{ position: 'absolute', top: 10, right: 10, width: 34, height: 34, borderRadius: '50%', background: 'rgba(255,255,255,.9)', display: 'grid', placeItems: 'center' }}>
                  <Icon name="favorite" size={18} color="var(--secondary)" />
                </div>
              </div>
              <div style={{ padding: '12px 14px 14px' }}>
                <div className="t-title-md" style={{ color: 'var(--on-surface)' }}>{s.n}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginTop: 4 }}>
                  <Icon name="place" size={15} color="var(--on-surface-variant)" />
                  <span className="t-body-sm" style={{ color: 'var(--on-surface-variant)' }}>{s.v}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 8 }}>
                  <Icon name="star" size={16} fill={1} color="var(--tertiary)" />
                  <span className="t-label-lg" style={{ color: 'var(--on-surface)' }}>{s.r}</span>
                  <span className="t-body-sm" style={{ color: 'var(--on-surface-variant)' }}>(214)</span>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Bandeau réservation rapide */}
        <div style={{ margin: '24px 20px 8px', borderRadius: 'var(--radius-xl)', overflow: 'hidden', position: 'relative', background: 'linear-gradient(135deg, var(--primary), #4f3a5b)', padding: '22px 22px' }}>
          <div className="t-overline" style={{ color: 'rgba(255,255,255,.7)' }}>File d'attente en direct</div>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: 22, fontWeight: 600, color: '#fff', marginTop: 4, maxWidth: 230, lineHeight: 1.1 }}>Salon Lumière vous reçoit dans ~15 min</div>
          <Btn kind="primary" icon="login" style={{ marginTop: 16, background: '#fff', color: 'var(--primary)', height: 44 }}>Rejoindre la file</Btn>
        </div>
      </div>

      <BottomNav active={0} />
    </div>
  );
}

function SectionHead({ title, action = 'Voir tout' }) {
  return (
    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', padding: '0 20px' }}>
      <h3 style={{ fontFamily: 'var(--font-display)', fontSize: 22, fontWeight: 600, color: 'var(--on-surface)' }}>{title}</h3>
      <span className="t-label-lg" style={{ color: 'var(--primary)' }}>{action}</span>
    </div>
  );
}

Object.assign(window, { HomeScreen, SectionHead });
