// screen-salon.jsx — Détail d'un salon (onglet Services)
function SalonScreen() {
  const services = [
    { n: 'Coupe & brushing femme', c: 'COUPE', d: '45 min', p: '45' },
    { n: 'Balayage + patine', c: 'COLORATION', d: '2 h 30', p: '140' },
    { n: 'Soin profond Olaplex', c: 'SOIN', d: '30 min', p: '38' },
    { n: 'Coupe homme + barbe', c: 'BARBE', d: '40 min', p: '42' },
  ];
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: 'var(--background)', position: 'relative' }}>
      <div style={{ flex: 1, overflow: 'auto' }}>
        {/* Cover */}
        <div style={{ position: 'relative' }}>
          <Ph label="couverture salon · plein cadre" h={230} r={0} tone="primary" />
          <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, rgba(40,23,51,.4) 0%, transparent 30%, transparent 60%, rgba(40,23,51,.25) 100%)' }} />
          <div style={{ position: 'absolute', top: 12, left: 8, right: 8, display: 'flex', alignItems: 'center' }}>
            <button style={{ ...iconBtn, background: 'rgba(255,255,255,.85)' }}><Icon name="arrow_back" /></button>
            <div style={{ flex: 1 }} />
            <button style={{ ...iconBtn, background: 'rgba(255,255,255,.85)' }}><Icon name="share" size={22} /></button>
            <button style={{ ...iconBtn, background: 'rgba(255,255,255,.85)' }}><Icon name="favorite" size={22} color="var(--secondary)" /></button>
          </div>
        </div>

        {/* Identité salon */}
        <div style={{ background: 'var(--surface)', borderRadius: '24px 24px 0 0', marginTop: -22, position: 'relative', padding: '22px 20px 0' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
            <div style={{ flex: 1 }}>
              <h1 style={{ fontFamily: 'var(--font-display)', fontSize: 30, fontWeight: 600, lineHeight: 1.02, color: 'var(--on-surface)' }}>Salon Lumière</h1>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 8 }}>
                <Icon name="place" size={16} color="var(--on-surface-variant)" />
                <span className="t-body-md" style={{ color: 'var(--on-surface-variant)' }}>12 rue de Châteaudun, Paris 9e</span>
              </div>
            </div>
            <div style={{ textAlign: 'center', background: 'var(--tertiary-container)', borderRadius: 'var(--radius-md)', padding: '8px 12px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                <Icon name="star" size={18} fill={1} color="var(--tertiary)" />
                <span className="t-title-md" style={{ color: 'var(--on-tertiary-container)' }}>4,9</span>
              </div>
              <div className="t-body-sm" style={{ color: 'var(--on-tertiary-container)' }}>214 avis</div>
            </div>
          </div>

          {/* Bandeau file d'attente */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 16, padding: '12px 14px', borderRadius: 'var(--radius-md)', background: 'var(--success-container)' }}>
            <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--success)', boxShadow: '0 0 0 4px rgba(76,122,87,.2)' }} />
            <span className="t-body-md" style={{ color: 'var(--on-success-container)', flex: 1, fontWeight: 600 }}>File ouverte · ~15 min d'attente</span>
            <span className="t-label-lg" style={{ color: 'var(--success)' }}>3 en attente</span>
          </div>

          {/* Onglets */}
          <div style={{ display: 'flex', gap: 22, marginTop: 18, borderBottom: '1px solid var(--outline-variant)', overflow: 'auto' }}>
            {['Services', 'Équipe', 'Avis', 'Posts', 'Info'].map((t, i) => (
              <div key={i} style={{ padding: '10px 0', position: 'relative', whiteSpace: 'nowrap' }}>
                <span className="t-label-lg" style={{ color: i === 0 ? 'var(--primary)' : 'var(--on-surface-variant)' }}>{t}</span>
                {i === 0 && <div style={{ position: 'absolute', bottom: -1, left: 0, right: 0, height: 3, borderRadius: 3, background: 'var(--primary)' }} />}
              </div>
            ))}
          </div>
        </div>

        {/* Liste des prestations */}
        <div style={{ background: 'var(--surface)', padding: '8px 20px 120px' }}>
          {services.map((s, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '16px 0', borderBottom: i < services.length - 1 ? '1px solid var(--outline-variant)' : 'none' }}>
              <div style={{ flex: 1, minWidth: 0 }}>
                <span style={{ fontFamily: 'var(--font-sans)', fontSize: 10.5, fontWeight: 800, letterSpacing: 1, color: 'var(--secondary)' }}>{s.c}</span>
                <div className="t-title-sm" style={{ color: 'var(--on-surface)', fontSize: 15.5, marginTop: 3 }}>{s.n}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginTop: 5 }}>
                  <Icon name="schedule" size={15} color="var(--on-surface-variant)" />
                  <span className="t-body-sm" style={{ color: 'var(--on-surface-variant)' }}>{s.d}</span>
                  <span style={{ margin: '0 4px', color: 'var(--outline-variant)' }}>·</span>
                  <span className="t-title-sm" style={{ color: 'var(--on-surface)' }}>{s.p} €</span>
                </div>
              </div>
              <button style={{ height: 40, padding: '0 18px', borderRadius: 'var(--radius-full)', border: '1px solid var(--primary)', background: 'var(--surface)', color: 'var(--primary)', fontFamily: 'var(--font-sans)', fontWeight: 700, fontSize: 13.5, cursor: 'pointer' }}>Réserver</button>
            </div>
          ))}
        </div>
      </div>

      {/* Barre d'action flottante */}
      <div style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: '14px 16px 16px', background: 'linear-gradient(180deg, transparent, var(--surface) 28%)', display: 'flex', gap: 10 }}>
        <Btn kind="outline" icon="person_add" style={{ flex: '0 0 auto', background: 'var(--surface)' }}>Suivre</Btn>
        <Btn kind="primary" icon="calendar_month" full style={{ boxShadow: 'var(--elev-3)' }}>Réserver une prestation</Btn>
      </div>
    </div>
  );
}

Object.assign(window, { SalonScreen });
