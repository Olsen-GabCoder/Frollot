// screen-feed.jsx — Fil social Frollot
function FeedScreen() {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: 'var(--background)' }}>
      {/* Header + onglets */}
      <div style={{ background: 'var(--surface)', paddingTop: 6 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '0 8px', minHeight: 52 }}>
          <button style={iconBtn}><Icon name="menu" /></button>
          <div style={{ flex: 1, fontFamily: 'var(--font-display)', fontSize: 24, fontWeight: 600 }}>Fil social</div>
          <button style={iconBtn}><Icon name="search" /></button>
          <button style={iconBtn}><Icon name="add_circle" fill={0} color="var(--primary)" /></button>
        </div>
        <div style={{ display: 'flex', padding: '0 8px' }}>
          {['Tous', 'Suivis', 'Tendances'].map((t, i) => (
            <div key={i} style={{ flex: 1, textAlign: 'center', padding: '12px 0', position: 'relative' }}>
              <span className="t-label-lg" style={{ color: i === 0 ? 'var(--primary)' : 'var(--on-surface-variant)' }}>{t}</span>
              {i === 0 && <div style={{ position: 'absolute', bottom: 0, left: '28%', right: '28%', height: 3, borderRadius: 3, background: 'var(--primary)' }} />}
            </div>
          ))}
        </div>
        <div style={{ height: 1, background: 'var(--outline-variant)' }} />
      </div>

      {/* Feed */}
      <div style={{ flex: 1, overflow: 'auto', padding: '12px 0 16px', display: 'flex', flexDirection: 'column', gap: 12 }}>
        <PostCard
          name="Studio Métamorphose" type="Salon" verified date="il y a 2 h" tone="secondary"
          text="Transformation balayage caramel sur base brune ✨ Glow garanti pour l'été."
          tags={['#balayage', '#blondcaramel', '#transformation']}
          beforeAfter />
        <PostCard
          name="Léa · Coloriste" type="Coiffeuse" verified date="il y a 5 h" tone="primary"
          text="Mon astuce pour faire durer votre couleur : un shampoing sans sulfate et une eau tiède, jamais brûlante."
          tags={['#conseil', '#soincouleur']} />
      </div>

      <BottomNav active={1} />
    </div>
  );
}

function PostCard({ name, type, verified, date, text, tags = [], tone = 'primary', beforeAfter = false }) {
  return (
    <article style={{ background: 'var(--surface)', borderTop: '1px solid var(--outline-variant)', borderBottom: '1px solid var(--outline-variant)' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '14px 16px 12px' }}>
        <Avatar initials={name[0]} size={44} ring tone={tone} />
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
            <span className="t-title-sm" style={{ color: 'var(--on-surface)', fontSize: 15 }}>{name}</span>
            {verified && <Icon name="verified" size={16} fill={1} color="var(--primary)" />}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 1 }}>
            <span style={{ fontFamily: 'var(--font-sans)', fontSize: 11, fontWeight: 700, color: 'var(--secondary)', background: 'var(--secondary-container)', padding: '1px 8px', borderRadius: 999 }}>{type}</span>
            <span className="t-body-sm" style={{ color: 'var(--on-surface-variant)' }}>· {date}</span>
          </div>
        </div>
        <button style={iconBtn}><Icon name="more_horiz" size={22} color="var(--on-surface-variant)" /></button>
      </div>

      {/* Texte */}
      <div style={{ padding: '0 16px 12px' }}>
        <p className="t-body-md" style={{ color: 'var(--on-surface)', lineHeight: 1.55 }}>{text}</p>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 8 }}>
          {tags.map(t => <span key={t} className="t-label-md" style={{ color: 'var(--primary)', textTransform: 'none', letterSpacing: 0, fontWeight: 700 }}>{t}</span>)}
        </div>
      </div>

      {/* Média */}
      {beforeAfter ? (
        <div style={{ display: 'flex', gap: 3, position: 'relative' }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <Ph label="avant" h={260} r={0} tone="neutral" />
            <span style={baLabel}>AVANT</span>
          </div>
          <div style={{ flex: 1, position: 'relative' }}>
            <Ph label="après" h={260} r={0} tone="secondary" />
            <span style={{ ...baLabel, background: 'var(--secondary)' }}>APRÈS</span>
          </div>
          <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%,-50%)', width: 40, height: 40, borderRadius: '50%', background: 'rgba(255,255,255,.92)', display: 'grid', placeItems: 'center', boxShadow: 'var(--elev-3)' }}>
            <Icon name="swap_horiz" size={22} color="var(--primary)" />
          </div>
        </div>
      ) : (
        <Ph label="photo réalisation" h={300} r={0} tone={tone} />
      )}

      {/* Engagement */}
      <div style={{ display: 'flex', alignItems: 'center', padding: '12px 12px 14px', gap: 4 }}>
        <Engage icon="favorite" count="1 248" tone="var(--secondary)" />
        <Engage icon="chat_bubble" count="86" />
        <Engage icon="share" count="32" />
        <div style={{ flex: 1 }} />
        <button style={iconBtn}><Icon name="bookmark" size={22} color="var(--on-surface-variant)" /></button>
      </div>
    </article>
  );
}

function Engage({ icon, count, tone = 'var(--on-surface-variant)' }) {
  return (
    <button style={{ display: 'inline-flex', alignItems: 'center', gap: 6, background: 'none', border: 'none', cursor: 'pointer', padding: '8px 12px', borderRadius: 999 }}>
      <Icon name={icon} size={22} color={tone} />
      <span className="t-label-lg" style={{ color: 'var(--on-surface-variant)' }}>{count}</span>
    </button>
  );
}

const baLabel = {
  position: 'absolute', bottom: 12, left: 12,
  fontFamily: 'var(--font-sans)', fontSize: 10.5, fontWeight: 800, letterSpacing: 1.5,
  color: '#fff', background: 'var(--inverse-surface)', padding: '4px 10px', borderRadius: 999,
};

Object.assign(window, { FeedScreen, PostCard, Engage });
